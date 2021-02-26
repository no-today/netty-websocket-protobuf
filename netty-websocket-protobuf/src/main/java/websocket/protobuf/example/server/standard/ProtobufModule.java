package websocket.protobuf.example.server.standard;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * @author no-today
 * @date 2021/02/24 上午10:35
 */
public class ProtobufModule {

    @ChannelHandler.Sharable
    static class ProtobufMessageToMessageDecoder extends MessageToMessageDecoder<WebSocketFrame> {
        @Override
        protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
            if (msg instanceof BinaryWebSocketFrame) {
                ByteBuf content = msg.content();
                out.add(content);
                content.retain();
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }

    @ChannelHandler.Sharable
    static class ProtobufMessageToMessageEncoder extends MessageToMessageEncoder<MessageLiteOrBuilder> {
        @Override
        protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out) throws Exception {

            /*
             * 下面代码拷贝自: TCP ProtobufEncoder
             * 因为客户端不能直接解析 Protobuf 编码生成的二进制流, 所以我们需要包装成 WebSocket 二进制流
             */
            if (msg instanceof MessageLite) {
                out.add(wrappedWebSocketFrame(wrappedBuffer(((MessageLite) msg).toByteArray())));
                return;
            }
            if (msg instanceof MessageLite.Builder) {
                out.add(wrappedWebSocketFrame(wrappedBuffer(((MessageLite.Builder) msg).build().toByteArray())));
            }
        }

        private static WebSocketFrame wrappedWebSocketFrame(ByteBuf byteBuf) {
            return new BinaryWebSocketFrame(byteBuf);
        }
    }
}
