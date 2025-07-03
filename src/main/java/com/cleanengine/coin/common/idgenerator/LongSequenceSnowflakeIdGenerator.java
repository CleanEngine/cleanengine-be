package com.cleanengine.coin.common.idgenerator;

import com.cleanengine.coin.common.time.ClockHolder;

import java.time.LocalDateTime;

import static com.cleanengine.coin.common.CommonValues.BASE_EPOCH_TIME_MILLIS;

// 최상위 bit 미사용, timestamp 41bit(1ms), workerId 6bit, sequence 16bit = 64bit
public class LongSequenceSnowflakeIdGenerator implements LongIdGenerator {

    private static final long WORKER_ID_BITS = 6L;
    private static final long SEQUENCE_BITS = 16L;

    // 각 부분의 최대값
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    // 각 부분을 왼쪽으로 시프트할 범위
    private static final long WORKER_REQUIRED_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_REQUIRED_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    private final long workerId;
    private final ClockHolder clockHolder;

    private long sequence = 0L;
    private long lastTimestampMillis = -1L;

    // 기준 시점 (2025년 1월 1일)
    private final long epoch = BASE_EPOCH_TIME_MILLIS;

    public LongSequenceSnowflakeIdGenerator(long workerId, ClockHolder clockHolder) {
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
        long timestampMillis = clockHolder.getTimeMillis();

        if (timestampMillis < lastTimestampMillis) {
            throw new IllegalStateException("시간이 되돌아감, leap smear가 적용되었는지 체크");
        }

        // 마지막 단위 시간과 현재 단위 시간이 같다면 시퀀스 증가
        if (lastTimestampMillis == timestampMillis) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 0이라는 것은 최대값에 도달했다는 것. 시퀀스가 최대값을 넘으면 다음 단위시간(10ms) 대기
            if (sequence == 0) {
                timestampMillis = waitAndGetNextTime(lastTimestampMillis);
            }
        } else {
            // 시간이 다르면 시퀀스 초기화
            sequence = 0L;
        }

        lastTimestampMillis = timestampMillis;

        // 비트 연산을 통해 최종 ID 조합
        return ((timestampMillis - epoch) << TIMESTAMP_REQUIRED_SHIFT) |
                (workerId << WORKER_REQUIRED_SHIFT) |
                sequence;
    }

    @Override
    public LocalDateTime extractDateTime(long id) {
        long timeMillis = ((id >>> TIMESTAMP_REQUIRED_SHIFT) + epoch);
        return clockHolder.convertTimeMillisToDateTime(timeMillis);
    }

    protected long waitAndGetNextTime(long lastTimestampMillis) {
        long timestampMillis = clockHolder.getTimeMillis();

        while (timestampMillis <= lastTimestampMillis) {
            timestampMillis = clockHolder.getTimeMillis();
        }

        return timestampMillis;
    }
}
