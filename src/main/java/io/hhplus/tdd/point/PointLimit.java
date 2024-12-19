package io.hhplus.tdd.point;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointLimit {

    MAX_CHARGE_AMOUNT(100_000L),
    CHARGE_UNIT(1000L),
    MAX_BALANCE(1_000_000L),
    ;

    private final long limit;

}
