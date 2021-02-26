package websocket.protobuf.example.service;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;
import websocket.protobuf.example.protocol.ProtobufMessageModule;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 命令策略集
 *
 * @author no-today
 * @date 2021/02/25 下午2:11
 */
@Component
public class CommandStrategyFactory {

    private final Map<Integer, CommandExecutor<?>> commandHandlerMap;

    public CommandStrategyFactory(List<CommandExecutor<?>> commandExecutors) {
        this.commandHandlerMap = commandExecutors.stream()
                .collect(Collectors.toMap(CommandExecutor::command, e -> e));
    }

    public void execute(Channel channel, ProtobufMessageModule.Message message) {
        CommandExecutor<?> commandExecutor = commandHandlerMap.get(message.getCommand());
        if (commandExecutor == null) {
            throw new CommandNotFoundException("Command handler not found, command is " + message.getCommand());
        }

        commandExecutor.execute(channel, message);
    }
}
