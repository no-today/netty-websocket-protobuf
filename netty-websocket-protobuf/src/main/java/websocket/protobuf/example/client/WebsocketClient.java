package websocket.protobuf.example.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author no-today
 * @date 2021/03/05 下午2:32
 */
public class WebsocketClient {

    private Channel channel;
    private final URI uri;
    private final String scheme;
    private final String host;
    private final int port;

    public WebsocketClient(String endpoint) {
        try {
            uri = new URI(endpoint);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        scheme = uri.getScheme() != null ? uri.getScheme() : "ws";
        host = uri.getHost() != null ? uri.getHost() : "127.0.0.1";

        switch (scheme) {
            case "ws":
                port = 80;
                break;
            case "wss":
                port = 443;
                break;
            default:
                port = -1;
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Only WS(S) is supported.");
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public void open() throws InterruptedException {
        final SslContext sslCtx;
        if ("wss".equalsIgnoreCase(scheme)) {
            try {
                sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
            } catch (SSLException e) {
                throw new RuntimeException(e);
            }
        } else {
            sslCtx = null;
        }

        NioEventLoopGroup group = new NioEventLoopGroup();

        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        final WebSocketClientHandler handler =
                new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }
                        p.addLast(new HttpClientCodec());
                        p.addLast(new HttpObjectAggregator(8192));
                        p.addLast(WebSocketClientCompressionHandler.INSTANCE);
                        p.addLast(new IdleStateHandler(20, 20, 60));
                        p.addLast(handler);
                    }
                });

        channel = bootstrap.connect(uri.getHost(), port).sync().channel();
        handler.handshakeFuture().sync();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            group.shutdownGracefully().syncUninterruptibly();
        }));
    }
}
