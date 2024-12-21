package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.lock.LockManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPointService {

    private final UserPointTable userPointTable;

    private final LockManager lockManager;

    public UserPoint getPoint(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 아이디입니다.");
        }

        return userPointTable.selectById(id);
    }

    public UserPoint chargePoint(long id, long amount, long updateMillis) {
        if (id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 아이디입니다.");
        }

        return lockManager.withLock(id, () -> {
            UserPoint userPoint = userPointTable.selectById(id);
            UserPoint updatedUserPoint = userPoint.charge(amount, updateMillis);
            return userPointTable.insertOrUpdate(id, updatedUserPoint.point());
        });
    }

    public UserPoint usePoint(long id, long amount, long updateMillis) {
        if (id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 아이디입니다.");
        }

        return lockManager.withLock(id, () -> {
            UserPoint userPoint = userPointTable.selectById(id);
            UserPoint updatedUserPoint = userPoint.use(amount, updateMillis);
            return userPointTable.insertOrUpdate(id, updatedUserPoint.point());
        });
    }

}
