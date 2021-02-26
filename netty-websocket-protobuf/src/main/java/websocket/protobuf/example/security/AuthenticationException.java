package websocket.protobuf.example.security;

public abstract class AuthenticationException extends RuntimeException {

    public AuthenticationException(String msg, Throwable t) {
        super(msg, t);
    }

    public AuthenticationException(String msg) {
        super(msg);
    }
}
