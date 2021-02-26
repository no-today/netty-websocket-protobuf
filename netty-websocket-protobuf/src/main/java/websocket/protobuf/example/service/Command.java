package websocket.protobuf.example.service;

/**
 * @author no-today
 * @date 2021/02/26 上午10:06
 */
public enum Command {
    TEST(1, "测试命令");

    public final int command;
    public final String desc;

    Command(int command, String desc) {
        this.command = command;
        this.desc = desc;
    }
}
