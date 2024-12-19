package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;

@Service
@RequiredArgsConstructor
public class PointFacade {

    private final UserPointService userPointService;

    private final PointHistoryService pointHistoryService;

    public UserPoint chargePoint(long id, long amount, long updateMillis) {
        UserPoint changedUserPoint = userPointService.chargePoint(id, amount, updateMillis);
        pointHistoryService.saveHistory(id, amount, CHARGE, updateMillis);
        return changedUserPoint;
    }

    public UserPoint usePoint(long id, long amount, long updateMillis) {
        UserPoint changedUserPoint = userPointService.usePoint(id, amount, updateMillis);
        pointHistoryService.saveHistory(id, amount, USE, updateMillis);
        return changedUserPoint;
    }

}
