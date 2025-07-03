package com.cleanengine.coin.common.idgenerator;

import com.cleanengine.coin.common.time.ClockHolder;

// 기존 MIG 방식과는 달리 최상위 bit 미사용 (총 39bit)
public class HalfMigIdGenerator implements LongIdGenerator {

    private static final long WORKER_ID_BITS = 6L;
    private static final long SEQUENCE_BITS = 18L;

    // 각 부분의 최대값
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    // 각 부분을 왼쪽으로 시프트할 범위
    private static final long WORKER_REQUIRED_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_REQUIRED_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    private final long workerId;
    private final ClockHolder clockHolder;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    // 기준 시점 (2025년 1월 1일)
    private final long epoch = 173568960000L;

    public HalfMigIdGenerator(long workerId, ClockHolder clockHolder) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("workerId는 0부터 %d까지만 가능", MAX_WORKER_ID));
        }
        if(clockHolder == null) {
            throw new IllegalArgumentException("clockHolder는 null이 아니어야 한다.");
        }

        this.workerId = workerId;
        this.clockHolder = clockHolder;
    }

    public synchronized long nextId() {
        long tenMillisTimestamp = clockHolder.getTimeMillis()/10;

        if (tenMillisTimestamp < lastTimestamp) {
            throw new IllegalStateException("시간이 되돌아감, leap smear가 적용되었는지 체크");
        }

        // 마지막 단위 시간과 현재 단위 시간이 같다면 시퀀스 증가
        if (lastTimestamp == tenMillisTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 0이라는 것은 최대값에 도달했다는 것. 시퀀스가 최대값을 넘으면 다음 단위시간(10ms) 대기
            if (sequence == 0) {
                tenMillisTimestamp = waitAndGetNextTime(lastTimestamp);
            }
        } else {
            // 시간이 다르면 시퀀스 초기화
            sequence = 0L;
        }

        lastTimestamp = tenMillisTimestamp;

        // 비트 연산을 통해 최종 ID 조합
        return ((tenMillisTimestamp - epoch) << TIMESTAMP_REQUIRED_SHIFT) |
                (workerId << WORKER_REQUIRED_SHIFT) |
                sequence;
    }

    protected long waitAndGetNextTime(long lastTimestamp) {
        long tenMillisTimestamp = clockHolder.getTimeMillis()/10;

        while (tenMillisTimestamp <= lastTimestamp) {
            try{
                Thread.sleep(1);
                tenMillisTimestamp = clockHolder.getTimeMillis()/10;
            }
            catch (InterruptedException e){
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        return tenMillisTimestamp;
    }
}
