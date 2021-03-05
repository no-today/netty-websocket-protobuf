package websocket.protobuf.example.server.standard;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import websocket.protobuf.example.protocol.ProtobufMessageModule;
import websocket.protobuf.example.server.EventListener;

/**
 * @author no-today
 * @date 2021/02/24 上午10:55
 */
public class WebsocketServerHandler extends ChannelInboundHandlerAdapter {

    private final EventListener eventListener;

    public WebsocketServerHandler(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        eventListener.doOnClose(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ProtobufMessageModule.Message) {
            eventListener.doOnBinaryMessage(ctx.channel(), (ProtobufMessageModule.Message) msg);
            return;
        }
        if (msg instanceof TextWebSocketFrame) {
            eventListener.doOnTextMessage(ctx.channel(), (TextWebSocketFrame) msg);
            return;
        }
        if (msg instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(((PingWebSocketFrame) msg).content().retain()));
            return;
        }
        if (msg instanceof PongWebSocketFrame) {
            return;
        }
        if (msg instanceof CloseWebSocketFrame) {
            ctx.writeAndFlush(((CloseWebSocketFrame) msg).retainedDuplicate()).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        eventListener.doOnEvent(ctx.channel(), evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        eventListener.doOnError(ctx.channel(), cause);
    }
}
