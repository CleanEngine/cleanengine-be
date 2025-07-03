package com.cleanengine.coin.common.idgenerator;

import com.cleanengine.coin.common.time.ClockHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

// 알고리즘이 중요하므로 whitebox 테스트 위주로 작성
public class HalfMigIdGeneratorTest {
    private static final ClockHolder defaultTestClockHolder = new TestClockHolder();

    @DisplayName("생성 유효성 검증")
    @Nested
    class ConstructorTest {
        @DisplayName("음수인 workerId로 생성할 경우 IllegalArgumentException를 던진다.")
        @Test
        void createWithNegativeWorkerId_throwsIllegalArgumentException() {
            long negativeWorkerId = -1L;

            assertThrows(IllegalArgumentException.class, () -> new HalfMigIdGenerator(negativeWorkerId, defaultTestClockHolder));
        }

        @DisplayName("최대값 이상인 workerId로 생성할 경우 IllegalArgumentException를 던진다.")
        @Test
        void createWithAboveMaxWorkerId_throwsIllegalArgumentException() {
            long maxWorkerId = 63L;

            assertThrows(IllegalArgumentException.class, () -> new HalfMigIdGenerator(maxWorkerId + 1, defaultTestClockHolder));
        }

        @DisplayName("null인 clockHolder로 생성할 경우 IllegalArgumentException를 던진다.")
        @Test
        void createWithNullClockHolder_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> new HalfMigIdGenerator(0L, null));
        }

        @DisplayName("올바른 workerId와 ClockHolder를 전달시 IdGenerator가 정상 생성된다.")
        @Test
        void createWithValidParameters_createdSuccessfully() {
            HalfMigIdGenerator halfMigIdGenerator = new HalfMigIdGenerator(0L, defaultTestClockHolder);

            assertDoesNotThrow(() -> halfMigIdGenerator.nextId());
        }
    }

    @DisplayName("Id 생성 유효성 검증")
    @Nested
    class NextIdTest {
        @DisplayName("Id 생성을 통해 정상적으로 Id를 생성한다.")
        @Test
        void generateIdSuccessfully() {
            HalfMigIdGenerator halfMigIdGenerator = new HalfMigIdGenerator(0L, defaultTestClockHolder);

            assertDoesNotThrow(() -> halfMigIdGenerator.nextId());
        }

        @DisplayName("시간이 되돌아 갔을 경우, IllegalStateException을 던진다.")
        @Test
        void nextIdWhenTimeReversed_throwsIllegalStateException() {
            TestClockHolder testClockHolder = new TestClockHolder();
            HalfMigIdGenerator halfMigIdGenerator = new HalfMigIdGenerator(0L, testClockHolder);
            halfMigIdGenerator.nextId();

            testClockHolder.elapseTimeMillis(-10);

            assertThrows(IllegalStateException.class, () -> halfMigIdGenerator.nextId());
        }

        @DisplayName("같은 단위 시간 내에 sequence 최대 생성치에 도달한 경우, 다음 시간이 바뀔때까지 대기한다.")
        @Test
        void reachMaxSequenceInSameTimeRange_calledNextId_waitNextTimeRange() throws InterruptedException {
            // given
            long maxSequenceCount = 262144L;

            // maxSequence까지 id 생성
            TestClockHolder testClockHolder = new TestClockHolder();
            HalfMigIdGenerator halfMigIdGenerator = new HalfMigIdGenerator(0L, testClockHolder);
            for (int i = 0; i < maxSequenceCount; i++) {
                halfMigIdGenerator.nextId();
            }

            // when
            try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {

                // 가상 스레드에서 nextId 호출
                Callable<Long> task = () -> halfMigIdGenerator.nextId();
                Future<Long> future = executorService.submit(task);

                // 한번의 nextId로 2번의 getTimeMillis Count가 늘었다면 대기 상태인 것
                while(testClockHolder.getCallingGetTimeMillisCount() >= maxSequenceCount + 2) {
                    Thread.sleep(1);
                }

                // 시간이 지나지 않았기에 아직 대기중이어야 함
                assertFalse(executorService.isTerminated());

                // 단위 시간 경과시키기
                testClockHolder.elapseTimeMillis(10);

                // sequence 추출
                long newId = future.get();
                long newSequence = extractSequence(newId);

                // 대기후 처음으로 생성된 sequence는 0이어야 함
                assertEquals(0L, newSequence);
            }
            catch(ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        @DisplayName("같은 단위 시간 내에 여러 스레드가 동시에 id를 요청한 경우에도, 중복되지 않은 Id를 생성한다.")
        @Test
        void multipleThreadCallsNextIdInSameTimeRange_generateUniqueIds() {

            TestClockHolder testClockHolder = new TestClockHolder();
            HalfMigIdGenerator halfMigIdGenerator = new HalfMigIdGenerator(0L, testClockHolder);

            int threadCount = 100;
            CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount);
            ConcurrentSkipListSet<Long> resultIds = new ConcurrentSkipListSet<>();

            try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
                IntStream.range(0, threadCount)
                        .forEach(i ->
                                executorService.submit(() -> {
                                    try {
                                        cyclicBarrier.await();
                                        resultIds.add(halfMigIdGenerator.nextId());
                                    } catch (InterruptedException | BrokenBarrierException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                );
            }

            // set 기반 자료형이므로, threadCount 수만큼의 id가 생성되어야 함
            assertEquals(threadCount, resultIds.size());
        }
    }

    private long extractSequence(long id) {
        return id & ~(-1L << 18);
    }

    private static class TestClockHolder implements ClockHolder {
        private long timeMillis = 0L;
        private long callingGetTimeMillisCount = 0L;

        @Override
        public long getTimeMillis() {
            callingGetTimeMillisCount++;
            return timeMillis;
        }

        public void elapseTimeMillis(long millis){
            timeMillis += millis;
        }

        public long getCallingGetTimeMillisCount() {
            return callingGetTimeMillisCount;
        }
    }
}
