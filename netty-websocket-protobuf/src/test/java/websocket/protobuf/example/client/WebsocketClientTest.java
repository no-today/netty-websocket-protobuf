package websocket.protobuf.example.client;

import org.junit.jupiter.api.Test;

/**
 * @author no-today
 * @date 2021/03/05 下午1:37
 */
class WebsocketClientTest {

    @Test
    public void test() throws Exception {
        final String url = "wss://wss.meiqijiacheng.com";
        WebsocketClient websocketClient = new WebsocketClient(url);
        websocketClient.open();;
    }
}