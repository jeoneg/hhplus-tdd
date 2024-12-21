package io.hhplus.tdd.lock;

import java.util.function.Supplier;

public class FakeLockManager implements LockManager {

    @Override
    public <T> T withLock(Long key, Supplier<T> supplier) {
        return supplier.get();
    }

}
