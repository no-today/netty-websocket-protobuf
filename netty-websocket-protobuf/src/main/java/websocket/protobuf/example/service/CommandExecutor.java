package websocket.protobuf.example.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import websocket.protobuf.example.server.support.EnhanceChannelGroup;
import websocket.protobuf.example.protocol.ProtobufMessageModule;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;

/**
 * 命令执行者
 *
 * @author no-today
 * @date 2021/02/25 下午2:14
 */
public abstract class CommandExecutor<T extends Message> {

    private final int command;
    private final Class<T> dataClass;

    @Resource
    private EnhanceChannelGroup enhanceChannelGroup;

    public CommandExecutor(Command command) {
        this.command = command.command;
        this.dataClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public int command() {
        return command;
    }

    protected EnhanceChannelGroup channelGroup() {
        return enhanceChannelGroup;
    }

    public void execute(Channel channel, ProtobufMessageModule.Message message) {
        try {
            execute(channel, message, message.getData().unpack(dataClass));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected abstract void execute(Channel channel, ProtobufMessageModule.Message msg, T data);
}
