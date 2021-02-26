package websocket.protobuf.example.security;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 认证器
 *
 * @author no-today
 * @date 2021/02/26 下午4:57
 */
public interface Authenticator {

    /**
     * 执行认证
     *
     * @param channel channel
     * @param request request
     * @return cookie
     */
    Cookie authentication(Channel channel, FullHttpRequest request) throws AuthenticationException;
}
