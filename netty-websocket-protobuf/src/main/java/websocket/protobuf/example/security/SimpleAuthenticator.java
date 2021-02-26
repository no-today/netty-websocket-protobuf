package websocket.protobuf.example.security;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 基于Token认证
 * <p>
 * 参数可能存在 Query / Header
 *
 * @author no-today
 * @date 2021/02/26 下午5:55
 */
@Component
public class SimpleAuthenticator implements Authenticator {

    private final static GrantedAuthority ROLE_ANONYMOUS = new SimpleGrantedAuthority("Anonymous");
    private final static GrantedAuthority ROLE_USER = new SimpleGrantedAuthority("User");

    /**
     * Token key
     */
    private final static String TOKEN_KEY = "token";

    /**
     * Subscribes key
     */
    private final static String SUBSCRIBES_KEY = "subscribes";

    @Override
    public Cookie authentication(Channel channel, FullHttpRequest request) throws AuthenticationException {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

        String token = Optional.ofNullable(decoder.parameters().get(TOKEN_KEY)).map(e -> e.isEmpty() ? null : e.get(0)).orElse(request.headers().get(TOKEN_KEY));
        List<String> subscribes = Optional.ofNullable(decoder.parameters().get(SUBSCRIBES_KEY)).orElse(request.headers().getAll(SUBSCRIBES_KEY));

        if (token == null || token.trim().isEmpty()) {
            return new Cookie(channel, channel.id().asShortText(), null, Collections.singletonList(ROLE_ANONYMOUS));
        } else {
            return new Cookie(channel, String.valueOf(token.hashCode()), token, Collections.singletonList(ROLE_USER), true);
        }
    }
}
