package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPointServiceTest {

    @InjectMocks
    private UserPointService sut;

    @Mock
    private UserPointTable userPointTable;

    @Test
    void 포인트를_조회할_때_사용자_아이디가_유효하지_않으면_IllegalArgumentException을_반환한다() {
        // given
        long userId = 0L;

        // when then
        assertThatThrownBy(() -> sut.getPoint(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 사용자 아이디입니다.");

        verify(userPointTable, never()).selectById(userId);
    }

    @Test
    void 포인트를_조회하면_1000포인트가_조회된다() {
        // given
        long userId = 1L;
        long balance = 1000L;
        when(userPointTable.selectById(userId)).thenReturn(createUserPoint(userId, balance, System.currentTimeMillis()));

        // when
        UserPoint result = sut.getPoint(userId);

        // then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(balance);

        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    void 포인트를_충전할_때_사용자_아이디가_유효하지_않으면_IllegalArgumentException을_반환한다() {
        // given
        long userId = 0L;
        long chargingPoint = 1000L;
        long updateMillis = System.currentTimeMillis();

        // when then
        assertThatThrownBy(() -> sut.chargePoint(userId, chargingPoint, updateMillis))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 사용자 아이디입니다.");

        verify(userPointTable, never()).selectById(userId);
    }

    @Test
    void 포인트를_충전할_때_0포인트를_충전하면_IllegalArgumentException을_반환한다() {
        // given
        long userId = 1L;
        long chargingPoint = 0L;
        long updateMillis = System.currentTimeMillis();
        when(userPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));

        // when then
        assertThatThrownBy(() -> sut.chargePoint(userId, chargingPoint, updateMillis))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전할 포인트는 0보다 커야 합니다.");

        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    void 포인트를_충전할_때_0보다_작은_포인트를_충전하면_IllegalArgumentException을_반환한다() {
        // given
        long userId = 1L;
        long chargingPoint = -1000L;
        long updateMillis = System.currentTimeMillis();
        when(userPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));

        // when then
        assertThatThrownBy(() -> sut.chargePoint(userId, chargingPoint, updateMillis))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전할 포인트는 0보다 커야 합니다.");

        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    void 포인트를_충전할_때_500포인트를_충전하면_IllegalArgumentException을_반환한다() {
        // given
        long userId = 1L;
        long chargingPoint = 500L;
        long updateMillis = System.currentTimeMillis();
        when(userPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));

        // when then
        assertThatThrownBy(() -> sut.chargePoint(userId, chargingPoint, updateMillis))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트 충전은 1000원 단위로 가능합니다.");

        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    void 포인트를_충전할_때_101_000포인트를_충전하면_IllegalArgumentException을_반환한다() {
        // given
        long userId = 1L;
        long chargingPoint = 101_000L;
        long updateMillis = System.currentTimeMillis();
        when(userPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));

        // when then
        assertThatThrownBy(() -> sut.chargePoint(userId, chargingPoint, updateMillis))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("한 번에 충전할 수 있는 최대 포인트는 10만 포인트입니다.");

        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    void 포인트를_충전할_때_총_포인트가_1_001_000포인트이면_IllegalArgumentException을_반환한다() {
        // given
        long userId = 1L;
        long balance = 1_000_000L;
        long chargingPoint = 1000L;
        long updateMillis = System.currentTimeMillis();
        when(userPointTable.selectById(userId)).thenReturn(createUserPoint(userId, balance, System.currentTimeMillis()));

        // when then
        assertThatThrownBy(() -> sut.chargePoint(userId, chargingPoint, updateMillis))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("보유 가능한 최대 포인트는 100만 포인트입니다.");

        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    void 포인트를_충전할_때_2000포인트를_충전하면_총_3000포인트가_된다() {
        // given
        long userId = 1L;
        long balance = 1000L;
        long chargingPoint = 2000L;
        long updateMillis = System.currentTimeMillis();
        when(userPointTable.selectById(userId)).thenReturn(createUserPoint(userId, balance, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(userId, balance + chargingPoint)).thenReturn(createUserPoint(userId, balance + chargingPoint, updateMillis));

        // when
        UserPoint result = sut.chargePoint(userId, chargingPoint, updateMillis);

        //then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(3000L);
        assertThat(result.updateMillis()).isEqualTo(updateMillis);

        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, times(1)).insertOrUpdate(userId, 3000L);
    }

    private UserPoint createUserPoint(long id, long point, long updateMillis) {
        return new UserPoint(id, point, updateMillis);
    }

}