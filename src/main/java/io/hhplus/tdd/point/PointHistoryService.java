package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointHistoryTable pointHistoryTable;

    public List<PointHistory> getPointHistories(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 아이디입니다.");
        }

        return pointHistoryTable.selectAllByUserId(id);
    }

}
