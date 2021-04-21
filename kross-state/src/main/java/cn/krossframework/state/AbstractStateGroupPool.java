package cn.krossframework.state;

import cn.krossframework.commons.thread.AutoTask;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractStateGroupPool implements StateGroupPool {

    private static final Logger log = LoggerFactory.getLogger(AbstractStateGroupPool.class);

    private static final AtomicLong ID_COUNT = new AtomicLong(0);

    protected final StateGroupFactory stateGroupFactory;

    protected volatile BlockingQueue<Long> groupIdRecycleQueue;

    protected volatile ConcurrentHashMap<Long, StateGroup> stateGroupMap;

    /**
     * remove deposed stateGroup period
     */
    private Long period;

    public AbstractStateGroupPool(StateGroupFactory stateGroupFactory) {
        Preconditions.checkNotNull(stateGroupFactory);
        this.stateGroupFactory = stateGroupFactory;
        this.stateGroupMap = new ConcurrentHashMap<>();
        this.groupIdRecycleQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public StateGroupFactory getStateGroupFactory() {
        return this.stateGroupFactory;
    }

    @Override
    public void setRemoveDeposedStateGroupPeriod(long period) {
        this.period = period;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new AutoTask(this.period == null ? 0 : this.period, 2) {
            @Override
            protected void run() {
                AbstractStateGroupPool.this.removeDeposedStateGroup();
            }
        }.start();
    }

    @Override
    public void removeDeposedStateGroup() {
        final ConcurrentHashMap<Long, StateGroup> stateGroupMap = this.stateGroupMap;
        if (stateGroupMap.size() == 0) {
            return;
        }
        log.info("start remove deposed stateGroup, current size: {}", stateGroupMap.size());
        final BlockingQueue<Long> groupIdRecycleQueue = new LinkedBlockingQueue<>();
        stateGroupMap.entrySet().removeIf(e -> {
            boolean b = e.getValue().canDeposed();
            if (b) {
                groupIdRecycleQueue.offer(e.getKey());
                log.info("stateGroup can be deposed, id: {}", e.getKey());
            }
            return b;
        });
        this.stateGroupMap = stateGroupMap;
        this.groupIdRecycleQueue = groupIdRecycleQueue;
        log.info("end remove deposed stateGroup, current size: {}", this.stateGroupMap.size());
    }

    protected long getNextGroupId() {
        Long id;
        return (id = this.groupIdRecycleQueue.poll()) == null ? ID_COUNT.incrementAndGet() : id;
    }

    @Override
    public FetchStateGroup findOrCreate(Long id, final StateGroupConfig stateGroupConfig) {
        if (id == null) {
            id = this.getNextGroupId();
        }
        boolean[] isNew = {false};
        final ConcurrentHashMap<Long, StateGroup> stateGroupMap = this.stateGroupMap;
        StateGroup stateGroup = stateGroupMap.computeIfAbsent(id, (i) -> {
            isNew[0] = true;
            if (stateGroupConfig == null) {
                throw new NullPointerException("stateGroupConfig can not be null when create new stateGroup");
            }
            return this.getStateGroupFactory().create(i, stateGroupConfig);
        });
        log.info("find or create stateGroup, stateGroup id: {}, isNew: {}", stateGroup.getId(), isNew[0]);
        this.stateGroupMap = stateGroupMap;
        return new FetchStateGroup(isNew[0], stateGroup);
    }

    @Override
    public StateGroup find(long id) {
        return this.stateGroupMap.get(id);
    }
}
