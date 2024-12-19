package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.hhplus.tdd.point.TransactionType.CHARGE;

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

}
