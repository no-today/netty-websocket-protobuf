package websocket.protobuf.example.server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import websocket.protobuf.example.protocol.ProtobufMessageModule;
import websocket.protobuf.example.security.Authenticator;
import websocket.protobuf.example.server.support.ChannelAttr;
import websocket.protobuf.example.service.CommandStrategyFactory;

/**
 * @author no-today
 * @date 2021/02/25 下午2:46
 */
@Slf4j
@Component
public class DefaultServerEventListener implements ServerEventListener {

    private final CommandStrategyFactory commandStrategyFactory;
    private final ChannelGroup channelGroup;
    private final Authenticator authenticator;

    public DefaultServerEventListener(CommandStrategyFactory commandStrategyFactory, ChannelGroup channelGroup, Authenticator authenticator) {
        this.commandStrategyFactory = commandStrategyFactory;
        this.channelGroup = channelGroup;
        this.authenticator = authenticator;
    }

    @Override
    public void doBeforeHandshake(Channel channel, FullHttpRequest req) {
        ChannelAttr.setSession(channel, authenticator.authentication(channel, req));
        channelGroup.add(channel);

        log.debug("[BeforeHandshake]: uid: {}", ChannelAttr.getSession(channel).getPrincipal());
    }

    @Override
    public void doOnOpen(Channel channel, FullHttpRequest req) {
        log.debug("[OnOpen]: uid: {}", ChannelAttr.getSession(channel).getPrincipal());
    }

    @Override
    public void doOnClose(Channel channel) {
        log.debug("[OnClose]: uid: {}", ChannelAttr.getSession(channel).getPrincipal());
    }

    @Override
    public void doOnError(Channel channel, Throwable throwable) {
        log.error("[OnError]: uid: {}", ChannelAttr.getSession(channel).getPrincipal(), throwable);
    }

    @Override
    public void doOnEvent(Channel channel, Object event) {
        log.debug("[OnEvent]: uid: {}, event: {}", ChannelAttr.getSession(channel).getPrincipal(), event);
    }

    @Override
    public void doOnTextMessage(Channel channel, TextWebSocketFrame frame) {
        log.debug("[OnTextMessage]: uid: {}, text: {}", ChannelAttr.getSession(channel).getPrincipal(), frame.text());
    }

    @Override
    public void doOnBinaryMessage(Channel channel, ProtobufMessageModule.Message frame) {
        log.debug("[OnBinaryMessage]: uid: {}, command: {}", ChannelAttr.getSession(channel).getPrincipal(), frame.getCommand());
        commandStrategyFactory.execute(channel, frame);
    }
}
