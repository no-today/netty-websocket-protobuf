package websocket.protobuf.example.server.support;

import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.internal.PlatformDependent;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * 扩展根据 String 获取 Channel
 *
 * @author no-today
 * @date 2021/02/25 下午6:59
 */
public class EnhanceChannelGroup extends DefaultChannelGroup {

    private final ConcurrentMap<String, Channel> channels = PlatformDependent.newConcurrentHashMap();

    public EnhanceChannelGroup(EventExecutor executor) {
        super(executor);
    }

    public EnhanceChannelGroup(String name, EventExecutor executor) {
        super(name, executor);
    }

    public EnhanceChannelGroup(EventExecutor executor, boolean stayClosed) {
        super(executor, stayClosed);
    }

    public EnhanceChannelGroup(String name, EventExecutor executor, boolean stayClosed) {
        super(name, executor, stayClosed);
    }

    @Override
    public boolean add(Channel channel) {
        boolean added = super.add(channel);
        if (added) {
            channels.put(ChannelAttr.getSession(channel).getPrincipal(), channel);
        }
        return added;
    }

    @Override
    public boolean remove(Object o) {
        boolean removed = super.remove(o);
        if (removed && o instanceof Channel) {
            channels.remove(ChannelAttr.getSession((Channel) o).getPrincipal());
        }
        return removed;
    }

    public Optional<Channel> find(String key) {
        return Optional.ofNullable(channels.get(key));
    }
}
