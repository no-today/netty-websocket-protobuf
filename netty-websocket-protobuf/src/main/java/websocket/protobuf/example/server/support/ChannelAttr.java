package websocket.protobuf.example.server.support;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import websocket.protobuf.example.security.Cookie;

/**
 * @author no-today
 * @date 2021/02/26 上午10:16
 */
public class ChannelAttr {

    private static final AttributeKey<Cookie> SESSION_KEY = AttributeKey.valueOf("WEBSOCKET_SESSION");

    public static Cookie getSession(Channel channel) {
        return channel.attr(SESSION_KEY).get();
    }

    public static void setSession(Channel channel, Cookie session) {
        channel.attr(SESSION_KEY).set(session);
    }
}
