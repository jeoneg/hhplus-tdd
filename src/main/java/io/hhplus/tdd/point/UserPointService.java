package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPointService {

    private final UserPointTable userPointTable;

    public UserPoint getPoint(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 아이디입니다.");
        }

        return userPointTable.selectById(id);
    }

}
