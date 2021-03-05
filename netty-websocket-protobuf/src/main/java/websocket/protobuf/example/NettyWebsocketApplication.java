package websocket.protobuf.example;

import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import websocket.protobuf.example.server.EventListener;
import websocket.protobuf.example.server.config.ServerEndpointConfig;
import websocket.protobuf.example.server.standard.WebsocketServer;
import websocket.protobuf.example.server.support.EnhanceChannelGroup;

import javax.annotation.Resource;

@SpringBootApplication
public class NettyWebsocketApplication implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private EventListener eventListener;

    public static void main(String[] args) {
        SpringApplication.run(NettyWebsocketApplication.class, args);
    }

    @Bean
    public EnhanceChannelGroup enhanceChannelGroup() {
        return new EnhanceChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        new WebsocketServer(eventListener, ServerEndpointConfig.defaultConfig()).init();
    }
}
