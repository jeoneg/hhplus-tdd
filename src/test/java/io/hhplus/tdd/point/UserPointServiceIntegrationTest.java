package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserPointServiceIntegrationTest {

    @Autowired
    private UserPointService sut;

    @Autowired
    private UserPointTable userPointTable;

    @BeforeEach
    void tearDown() {
        userPointTable.insertOrUpdate(1L, 0L);
        userPointTable.insertOrUpdate(2L, 0L);
    }

    @Test
    void 동시에_1000포인트_충전_요청을_5번_보내면_총_5000포인트가_된다() throws InterruptedException {
        // given
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    sut.chargePoint(1L, 1000L, System.currentTimeMillis());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        UserPoint result = userPointTable.selectById(1L);
        assertThat(result.point()).isEqualTo(5000L);
    }

    @Test
    void 동시에_1000포인트_사용_요청을_5번_보내면_총_0포인트가_된다() throws InterruptedException {
        // given
        long userId = 1L;
        userPointTable.insertOrUpdate(userId, 5000L);

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    sut.usePoint(userId, 1000L, System.currentTimeMillis());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        UserPoint result = userPointTable.selectById(userId);
        assertThat(result.point()).isEqualTo(0L);
    }

    @Test
    void 동시에_1000포인트_충전_요청_5번_500포인트_사용_요청_5번을_보내면_총_12500포인트가_된다() throws InterruptedException {
        // given
        long userId = 1L;
        userPointTable.insertOrUpdate(userId, 10000L);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        IntStream.range(0, threadCount).forEach(i -> {
            executorService.submit(() -> {
                try {
                    if (i % 2 == 0) {
                        sut.chargePoint(userId, 1000L, System.currentTimeMillis());
                    } else {
                        sut.usePoint(userId, 500L, System.currentTimeMillis());
                    }
                } finally {
                    latch.countDown();
                }
            });
        });

        latch.await();

        // then
        UserPoint result = userPointTable.selectById(userId);
        assertThat(result.point()).isEqualTo(12500L);
    }

    @Test
    void 동시에_두명의_사용자가_각자_1000포인트_충전_요청을_5번씩_보내면_각자_총_5000포인트가_된다() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        IntStream.range(0, threadCount).forEach(i -> {
            executorService.submit(() -> {
                try {
                    if (i % 2 == 0) {
                        sut.chargePoint(1L, 1000L, System.currentTimeMillis());
                    } else {
                        sut.chargePoint(2L, 1000L, System.currentTimeMillis());
                    }
                } finally {
                    latch.countDown();
                }
            });
        });

        latch.await();

        // then
        UserPoint result1 = userPointTable.selectById(1L);
        assertThat(result1.point()).isEqualTo(5000L);

        UserPoint result2 = userPointTable.selectById(2L);
        assertThat(result2.point()).isEqualTo(5000L);
    }

}
