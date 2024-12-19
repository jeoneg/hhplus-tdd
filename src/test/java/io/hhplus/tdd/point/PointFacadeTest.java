package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointFacadeTest {

    @InjectMocks
    private PointFacade sut;

    @Mock
    private UserPointService userPointService;

    @Mock
    private PointHistoryService pointHistoryService;

    @Test
    void 포인트를_충전할_때_1000포인트를_충전하면_총_3000포인트가_되고_충전_이력이_저장된다() {
        // given
        long userId = 1L;
        long balance = 2000L;
        long chargingPoint = 1000L;
        long updateMillis = System.currentTimeMillis();
        when(userPointService.chargePoint(userId, chargingPoint, updateMillis)).thenReturn(createUserPoint(userId, balance + chargingPoint, updateMillis));

        // when
        UserPoint result = sut.chargePoint(userId, chargingPoint, updateMillis);

        // then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(3000L);
        assertThat(result.updateMillis()).isEqualTo(updateMillis);

        verify(pointHistoryService, times(1)).saveHistory(userId, 1000L, CHARGE, updateMillis);
    }

    private UserPoint createUserPoint(long id, long point, long updateMillis) {
        return new UserPoint(id, point, updateMillis);
    }

}