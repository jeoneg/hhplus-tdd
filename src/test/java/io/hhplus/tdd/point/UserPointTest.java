package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserPointTest {

    @Test
    void 포인트를_충전할_때_0포인트를_충전하면_IllegalArgumentException을_반환한다() {
        // given
        UserPoint sut = UserPoint.empty(1L);
        long chargingPoint = 0L;

        // when, then
        assertThatThrownBy(() -> sut.charge(chargingPoint, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전할 포인트는 0보다 커야 합니다.");
    }

    @Test
    void 포인트를_충전할_때_0보다_작은_포인트를_충전하면_IllegalArgumentException을_반환한다() {
        // given
        UserPoint sut = UserPoint.empty(1L);
        long chargingPoint = -1000L;

        // when then
        assertThatThrownBy(() -> sut.charge(chargingPoint, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전할 포인트는 0보다 커야 합니다.");
    }

    @Test
    void 포인트를_충전할_때_500포인트를_충전하면_IllegalArgumentException을_반환한다() {
        // given
        UserPoint sut = UserPoint.empty(1L);
        long chargingPoint = 500L;

        // when then
        assertThatThrownBy(() -> sut.charge(chargingPoint, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트 충전은 1000원 단위로 가능합니다.");
    }

    @Test
    void 포인트를_충전할_때_101_000포인트를_충전하면_IllegalArgumentException을_반환한다() {
        // given
        UserPoint sut = UserPoint.empty(1L);
        long chargingPoint = 101_000L;

        // when then
        assertThatThrownBy(() -> sut.charge(chargingPoint, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("한 번에 충전할 수 있는 최대 포인트는 10만 포인트입니다.");
    }

    @Test
    void 포인트를_충전할_때_총_포인트가_1_001_000포인트이면_IllegalArgumentException을_반환한다() {
        // given
        long balance = 1_000_000L;
        UserPoint sut = createUserPoint(1L, balance, System.currentTimeMillis());
        long chargingPoint = 1000L;

        // when then
        assertThatThrownBy(() -> sut.charge(chargingPoint, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("보유 가능한 최대 포인트는 100만 포인트입니다.");
    }

    @Test
    void 포인트를_충전할_때_2000포인트를_충전하면_총_3000포인트가_된다() {
        // given
        long balance = 1000L;
        UserPoint sut = createUserPoint(1L, balance, System.currentTimeMillis());
        long chargingPoint = 2000L;

        // when
        UserPoint result = sut.charge(chargingPoint, System.currentTimeMillis());

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.point()).isEqualTo(3000L);
    }

    @Test
    void 포인트를_사용할_때_0포인트를_사용하면_IllegalArgumentException을_반환한다() {
        // given
        UserPoint sut = UserPoint.empty(1L);
        long usingPoint = 0L;

        // when then
        assertThatThrownBy(() -> sut.use(usingPoint, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용할 포인트는 0보다 커야 합니다.");
    }

    @Test
    void 포인트를_사용할_때_0보다_작은_포인트를_사용하면_IllegalArgumentException을_반환한다() {
        // given
        UserPoint sut = UserPoint.empty(1L);
        long usingPoint = -1000L;

        // when then
        assertThatThrownBy(() -> sut.use(usingPoint, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용할 포인트는 0보다 커야 합니다.");
    }

    @Test
    void 포인트를_사용할_때_2000포인트를_사용하면_IllegalArgumentException을_반환한다() {
        // given
        long balance = 1000L;
        UserPoint sut = createUserPoint(1L, balance, System.currentTimeMillis());
        long usingPoint = 2000L;

        // when then
        assertThatThrownBy(() -> sut.use(usingPoint, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("보유한 포인트가 부족합니다.");
    }

    @Test
    void 포인트를_사용할_때_2000포인트를_사용하면_총_1000포인트가_된다() {
        // given
        long balance = 3000L;
        UserPoint sut = createUserPoint(1L, balance, System.currentTimeMillis());
        long usingPoint = 2000L;

        // when
        UserPoint result = sut.use(usingPoint, System.currentTimeMillis());

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.point()).isEqualTo(1000L);
    }

    private UserPoint createUserPoint(long id, long point, long updateMillis) {
        return new UserPoint(id, point, updateMillis);
    }

}