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

    private UserPoint createUserPoint(long id, long point, long updateMillis) {
        return new UserPoint(id, point, updateMillis);
    }

}