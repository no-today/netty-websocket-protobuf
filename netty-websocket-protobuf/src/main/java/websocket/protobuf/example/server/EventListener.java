package websocket.protobuf.example.server;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import websocket.protobuf.example.protocol.ProtobufMessageModule;

/**
 * 事件监听器
 *
 * @author no-today
 * @date 2021/02/25 下午2:32
 */
public interface EventListener {

    /**
     * 是否执行在握手之前触发的勾子函数
     *
     * @param channel channel
     * @return trigger or not
     */
    default boolean hasBeforeHandshake(Channel channel) {
        return true;
    }

    /**
     * 在握手之前触发, 通常用于身份认证
     *
     * @param channel channel
     * @param req     request
     */
    void doBeforeHandshake(Channel channel, FullHttpRequest req);

    /**
     * 连接打开时触发
     *
     * @param channel channel
     * @param req     request
     */
    void doOnOpen(Channel channel, FullHttpRequest req);

    /**
     * 连接关闭时触发
     *
     * @param channel channel
     */
    void doOnClose(Channel channel);

    /**
     * 异常时触发
     *
     * @param channel   channel
     * @param throwable throwable
     */
    void doOnError(Channel channel, Throwable throwable);

    /**
     * 事件触发
     *
     * @param channel channel
     * @param event   event
     */
    void doOnEvent(Channel channel, Object event);

    /**
     * 文本消息
     *
     * @param channel channel
     * @param frame   frame
     */
    void doOnTextMessage(Channel channel, TextWebSocketFrame frame);

    /**
     * 二进制消息触发(protobuf)
     *
     * @param channel channel
     * @param frame   frame
     */
    void doOnBinaryMessage(Channel channel, ProtobufMessageModule.Message frame);
}
