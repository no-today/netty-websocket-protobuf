package websocket.protobuf.example.context;

import org.springframework.stereotype.Component;
import websocket.protobuf.example.server.support.EnhanceChannelGroup;
import websocket.protobuf.example.protocol.ProtobufMessageModule;

/**
 * 服务上下文
 * 通过该类找到会话并对会话进行IO操作
 *
 * @author no-today
 * @date 2021/02/26 下午5:05
 */
@Component
public class ServerHandlerContext {

    private final EnhanceChannelGroup channelGroup;

    public ServerHandlerContext(EnhanceChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    public void write(String principal, ProtobufMessageModule.Message message) {
        channelGroup.find(principal).ifPresent(e -> e.writeAndFlush(message));
    }
}
