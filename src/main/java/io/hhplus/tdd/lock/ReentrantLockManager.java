package io.hhplus.tdd.lock;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class ReentrantLockManager implements LockManager {

    private final Map<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public <T> T withLock(Long id, Supplier<T> action) {
        ReentrantLock lock = lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                lockMap.remove(id);
            }
        }
    }

}
