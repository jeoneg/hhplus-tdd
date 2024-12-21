# 동시성 제어 방식에 대한 분석 및 보고서

## 문제

사용자가 포인트를 충전 및 사용 시 같은 공유 자원에 여러 스레드가 동시에 접근하여 수정하는 상황이 발생할 수 있습니다.
<br>
<br>
```상황 예시)``` 잔액이 1000포인트일 때, 스레드1이 잔액 1000포인트를 조회하고 3000포인트를 충전하려 하지만, 저장하기 전에 스레드2가 잔액 1000포인트를 조회하고 2000포인트를 충전하여 먼저
저장합니다. 이 경우, 총 6000포인트가 충전되어야 하지만 실제로는 4000포인트만 저장됩니다.

## 해결 방안

1. 임계 영역(포인트 조회 > 포인트 충전 > 포인트 저장)에 스레드 하나만 접근할 수 있도록 Synchronized를 사용했습니다.
2. 순차적으로 작업을 처리할 수 있도록 ReentrantLock을 도입했습니다.
3. 사용자별 작업을 독립적으로 처리할 수 있도록 ConcurrentHashMap을 활용했습니다.

## Synchronized 사용

### 코드 예시

```java
public UserPoint chargePoint(long id, long amount, long updateMillis) {
    synchronized (this) {
        UserPoint userPoint = userPointTable.selectById(id);
        UserPoint chargedUserPoint = userPoint.charge(amount, updateMillis);
        return userPointTable.insertOrUpdate(id, chargedUserPoint.point());
    }
}

```

### 장점

1. 모니터 락을 활용한 Synchronized를 사용하여 임계 영역을 보호함으로써 코드 블록에 하나의 스레드만 접근할 수 있도록 개선되었습니다.

### 단점

1. Synchronized는 락을 획득하는 순서를 보장해 주지 않기 때문에 락을 반납한 후 대기 중이던 스레드들 간의 경쟁으로 인해 순차적 처리를 보장할 수 없습니다.
2. Synchronized 블록 내에서는 스레드 인터럽트가 불가능하여 무한 대기 상태에 빠질 수 있습니다.
3. 모든 사용자의 작업이 하나의 락으로 관리되어 병목 현상이 발생할 수 있습니다.

## ReentrantLock 도입

### 코드 예시

```java
private final ReentrantLock lock = new ReentrantLock(true); // default: false(fairless)

public UserPoint chargePoint(long id, long amount, long updateMillis) {
    lock.lock();
    try {
        UserPoint userPoint = userPointTable.selectById(id);
        UserPoint chargedUserPoint = userPoint.charge(amount, updateMillis);
        return userPointTable.insertOrUpdate(id, chargedUserPoint.point());
    } finally {
        lock.unlock();
    }
}

```

### 개선 사항

1. ReentrantLock의 공정 모드는 락을 요청한 순서대로 스레드가 락을 획득할 수 있게 합니다. 스레드가 순차적으로 락을 획득할 수 있도록 개선되었습니다.

### 단점

1. ReentrantLock의 공정 모드는 대기 큐에 들어간 순서대로 락을 부여하여 공정성을 보장하지만 큐 관리 비용을 발생시켜 이로 인해 성능이 저하될 수 있습니다.

### 한계

1. 여전히 모든 사용자가 같은 락을 공유하여 병목 현상이 발생할 수 있습니다.
2. 사용자별 독립적인 작업 처리를 보장하지 못합니다.

## ConcurrentHashMap을 이용한 사용자별 락 구현

ConcurrentHashMap은 내부적으로 여러 개의 세그먼트로 나누어져 있습니다.
각 세그먼트는 독립적으로 잠금을 관리하여, 여러 스레드가 동시에 다른 세그먼트에 접근할 수 있도록 합니다.
즉 모든 읽기 및 쓰기 작업에 대해 thread-safe한 해시맵으로 다중 스레드 환경에서도 안전하게 사용할 수 있습니다.

<img width="883" alt="HashMap" src="https://github.com/user-attachments/assets/c4bca8fa-1c91-463e-a23c-0bc186731b79" />
<img width="903" alt="ConcurrentHashMap" src="https://github.com/user-attachments/assets/079ffb70-2429-4033-801a-729614c7e0cc" />

### 코드 예시

```java
private final Map<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

public UserPoint chargePoint(long id, long amount, long updateMillis) {
    return withLock(id, () -> {
        UserPoint userPoint = userPointTable.selectById(id);
        UserPoint updatedUserPoint = userPoint.charge(amount, updateMillis);
        return userPointTable.insertOrUpdate(id, updatedUserPoint.point());
    });
}

private <T> T withLock(long id, Supplier<T> action) {
    ReentrantLock lock = lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));
    lock.lock();
    try {
        return action.get();
    } finally {
        lock.unlock();
    }
}
```

### 개선 사항

1. 사용자별 ID에 대해 별도의 ReentrantLock 객체를 생성하여 관리함으로써 사용자별로 작업을 독립적으로 처리하면서 각 사용자에 대해 순차적 처리가 가능해졌습니다.

## 결론

<p>ConcurrentHashMap과 ReentrantLock을 활용하여 사용자의 작업을 독립적으로 처리하고 순차적으로 실행할 수 있도록 구현하였습니다.</p>
<p>하지만 현재의 구현 방식은 하나의 서버에서는 문제가 없지만 멀티 서버 환경에서는 프로세스들 사이에서는 락을 공유할 수 없기 때문에 동시성을 해결할 수 없습니다.
이처럼 멀티 서버 환경에서의 동시성 문제를 해결하기 위해서는 Redis라는 하나의 단일 서버에 위임하여 n개의 서버에 락을 걸어 해결할 수 있습니다.</p>