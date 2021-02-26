package websocket.protobuf.example.server.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Objects;

public class DefaultPathMatcher implements WsPathMatcher {

    private final String pattern;

    private DefaultPathMatcher(String pattern) {
        this.pattern = pattern;
    }

    public static WsPathMatcher of(String pattern) {
        return new DefaultPathMatcher(pattern);
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    @Override
    public boolean matchAndExtract(QueryStringDecoder decoder, Channel channel) {
        return pattern.equals(decoder.path());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultPathMatcher that = (DefaultPathMatcher) o;
        return Objects.equals(getPattern(), that.getPattern());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPattern());
    }
}
