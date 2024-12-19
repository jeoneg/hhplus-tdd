package io.hhplus.tdd.point;

import static io.hhplus.tdd.point.PointLimit.*;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint charge(long amount, long updateMillis) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전할 포인트는 0보다 커야 합니다.");
        }
        if (amount % CHARGE_UNIT.getLimit() != 0) {
            throw new IllegalArgumentException("포인트 충전은 1000원 단위로 가능합니다.");
        }
        if (amount > MAX_CHARGE_AMOUNT.getLimit()) {
            throw new IllegalArgumentException("한 번에 충전할 수 있는 최대 포인트는 10만 포인트입니다.");
        }
        if (this.point + amount > MAX_BALANCE.getLimit()) {
            throw new IllegalArgumentException("보유 가능한 최대 포인트는 100만 포인트입니다.");
        }

        long increasedAmount = this.point + amount;
        return new UserPoint(this.id, increasedAmount, updateMillis);
    }

    public UserPoint use(long amount, long updateMillis) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용할 포인트는 0보다 커야 합니다.");
        }
        if (this.point - amount < 0) {
            throw new IllegalArgumentException("보유한 포인트가 부족합니다.");
        }

        long deceasedAmount = this.point - amount;
        return new UserPoint(this.id, deceasedAmount, updateMillis);
    }

}
