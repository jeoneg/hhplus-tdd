package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

    @InjectMocks
    private PointHistoryService sut;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @Test
    void 포인트_이력을_조회할_때_사용자_아이디가_유효하지_않으면_IllegalArgumentException을_반환한다() {
        // given
        long id = 0L;

        // when then
        assertThatThrownBy(() -> sut.getPointHistories(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 사용자 아이디입니다.");

        verify(pointHistoryTable, never()).selectAllByUserId(id);
    }

    @Test
    void 포인트_이력을_조회하면_포인트_충전_사용_이력이_조회된다() {
        long id = 1L;
        List<PointHistory> pointHistories = List.of(
                createPointHistory(1L, id, 5000L, CHARGE, System.currentTimeMillis()),
                createPointHistory(2L, id, 3000L, USE, System.currentTimeMillis()),
                createPointHistory(3L, id, 2000L, USE, System.currentTimeMillis()),
                createPointHistory(4L, id, 1000L, CHARGE, System.currentTimeMillis())
        );
        when(pointHistoryTable.selectAllByUserId(id)).thenReturn(pointHistories);

        // when
        List<PointHistory> result = sut.getPointHistories(id);

        // then
        assertThat(result).hasSize(4)
                .extracting("id", "userId", "amount", "type")
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, 5000L, CHARGE),
                        tuple(2L, 1L, 3000L, USE),
                        tuple(3L, 1L, 2000L, USE),
                        tuple(4L, 1L, 1000L, CHARGE)
                );

        verify(pointHistoryTable, times(1)).selectAllByUserId(id);
    }

    private PointHistory createPointHistory(long id, long userId, long amount, TransactionType type, long updateMillis) {
        return new PointHistory(id, userId, amount, type, updateMillis);
    }

}