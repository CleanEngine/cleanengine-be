package com.cleanengine.coin.realitybot.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TicksTest {
    @Test
    void testToString() {
        // given
        Ticks tick = Ticks.builder()
                .market("KRW-BTC")
                .trade_date_utc("2025-06-01")
                .trade_time_utc("11:32:45")
                .timestamp("2025-06-01T11:32:45.789Z")
                .trade_price(1000.0f)
                .trade_volume(5.0)
                .prev_closing_price(980.0f)
                .change_price(20.0)
                .ask_bid("ASK")
                .sequential_id(1000001L)
                .build();

        // when
        String actual = tick.toString();

        // then
        String expected = "Ticks{" +
                "market='KRW-BTC', " +
                "trade_date_utc='2025-06-01', " +
                "trade_time_utc='11:32:45', " +
                "timestamp=2025-06-01T11:32:45.789Z, " +
                "trade_price=1000.0, " +
                "trade_volume=5.0, " +
                "prev_closing_price=980.0, " +
                "change_price=20.0, " +
                "ask_bid='ASK', " +
                "sequential_id=1000001" +
                "}";

        assertEquals(expected, actual);
    }
}