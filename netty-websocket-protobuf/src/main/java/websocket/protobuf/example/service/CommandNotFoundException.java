package websocket.protobuf.example.service;

/**
 * @author no-today
 * @date 2021/02/25 下午2:18
 */
public class CommandNotFoundException extends RuntimeException {

    public CommandNotFoundException(String message) {
        super(message);
    }
}
