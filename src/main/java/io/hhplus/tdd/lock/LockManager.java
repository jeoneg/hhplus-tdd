package io.hhplus.tdd.lock;

import java.util.function.Supplier;

public interface LockManager {

    <T> T withLock(Long key, Supplier<T> supplier);

}
