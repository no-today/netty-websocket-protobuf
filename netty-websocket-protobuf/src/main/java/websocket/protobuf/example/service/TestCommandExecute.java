package websocket.protobuf.example.service;

import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import websocket.protobuf.example.protocol.ProtobufMessageModule;

/**
 * Protobuf Any 使用 String 类型, 将内容原封不动的广播到所有通道
 *
 * @author no-today
 * @date 2021/02/25 下午3:02
 */
@Slf4j
@Component
public class TestCommandExecute extends CommandExecutor<StringValue> {

    public TestCommandExecute() {
        super(Command.TEST);
    }

    @Override
    public void execute(Channel channel, ProtobufMessageModule.Message msg, StringValue data) {
        channelGroup().writeAndFlush(ProtobufMessageModule.Message.newBuilder()
                .setCommand(msg.getCommand())
                .setHeader(msg.getHeader())
                .setData(Any.pack(StringValue.newBuilder().setValue(data.getValue()).build()))
                .build());
    }
}
