package websocket.protobuf.example.server.standard;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.util.StringUtils;
import websocket.protobuf.example.protocol.ProtobufMessageModule;
import websocket.protobuf.example.server.EventListener;
import websocket.protobuf.example.server.config.ServerEndpointConfig;
import websocket.protobuf.example.server.support.WsPathMatcher;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final EventListener eventListener;
    private final ServerEndpointConfig config;

    public HttpServerHandler(EventListener eventListener, ServerEndpointConfig config) {
        this.eventListener = eventListener;
        this.config = config;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        try {
            handleHttpRequest(ctx, msg);
        } catch (TypeMismatchException e) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            sendHttpResponse(ctx, msg, res);
            e.printStackTrace();
        } catch (Exception e) {
            FullHttpResponse res;
            res = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
            sendHttpResponse(ctx, msg, res);
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        eventListener.doOnError(ctx.channel(), cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        eventListener.doOnClose(ctx.channel());
        super.channelInactive(ctx);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        FullHttpResponse res;
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            sendHttpResponse(ctx, req, res);
            return;
        }

        // Allow only GET methods.
        if (req.method() != GET) {
            res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            sendHttpResponse(ctx, req, res);
            return;
        }

        HttpHeaders headers = req.headers();
        String host = headers.get(HttpHeaderNames.HOST);
        if (StringUtils.isEmpty(host)) {
            res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            sendHttpResponse(ctx, req, res);
            return;
        }

        if (!StringUtils.isEmpty(config.getHost()) && !config.getHost().equals("0.0.0.0") && !config.getHost().equals(host.split(":")[0])) {
            res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            sendHttpResponse(ctx, req, res);
            return;
        }

        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        String path = decoder.path();
        if ("/favicon.ico".equals(path)) {
            res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        Channel channel = ctx.channel();

        //path match
        String pattern = null;
        WsPathMatcher pathMatcher = config.getPATH();
        if (pathMatcher.matchAndExtract(decoder, channel)) {
            pattern = pathMatcher.getPattern();
        }

        if (pattern == null) {
            res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        if (!req.headers().contains(UPGRADE) || !req.headers().contains(SEC_WEBSOCKET_KEY) || !req.headers().contains(SEC_WEBSOCKET_VERSION)) {
            res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            sendHttpResponse(ctx, req, res);
            return;
        }

        String subprotocols = null;

        if (eventListener.hasBeforeHandshake(channel)) {
            eventListener.doBeforeHandshake(channel, req);

            if (!channel.isActive()) {
                return;
            }

            AttributeKey<String> subprotocolsAttrKey = AttributeKey.valueOf("subprotocols");
            if (channel.hasAttr(subprotocolsAttrKey)) {
                subprotocols = ctx.channel().attr(subprotocolsAttrKey).get();
            }
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), subprotocols, true, config.getMaxFramePayloadLength());
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
        } else {
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.remove(ctx.name());

            pipeline.addLast(new ProtobufEncoder());
            pipeline.addLast(new ProtobufModule.ProtobufMessageToMessageEncoder());

            if (config.getReaderIdleTimeSeconds() != 0 || config.getWriterIdleTimeSeconds() != 0 || config.getAllIdleTimeSeconds() != 0) {
                pipeline.addLast(new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds()));
            }
            if (config.isUseCompressionHandler()) {
                pipeline.addLast(new WebSocketServerCompressionHandler());
            }

            pipeline.addLast(new ProtobufVarint32FrameDecoder());
            pipeline.addLast(new ProtobufModule.ProtobufMessageToMessageDecoder());
            pipeline.addLast(new ProtobufDecoder(ProtobufMessageModule.Message.getDefaultInstance()));
            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());

            pipeline.addLast(new WebSocketFrameAggregator(Integer.MAX_VALUE));
            pipeline.addLast(new WebsocketServerHandler(eventListener));

            handshaker.handshake(channel, req).addListener(future -> {
                if (future.isSuccess()) {
                    eventListener.doOnOpen(channel, req);
                } else {
                    handshaker.close(channel, new CloseWebSocketFrame());
                }
            });
        }
    }

    public static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        int statusCode = res.status().code();
        if (statusCode != OK.code() && res.content().readableBytes() == 0) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        HttpUtil.setContentLength(res, res.content().readableBytes());

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || statusCode != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST) + req.uri();
        return "ws://" + location;
    }
}