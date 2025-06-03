package com.cleanengine.coin.realitybot.parser;

import com.cleanengine.coin.realitybot.dto.Ticks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TickParserTest {
    private TickParser tickParser;

    @BeforeEach
    void setUp() {
        tickParser = new TickParser();
    }

    @Test
    @DisplayName("json을 주면 ticks 객체를 반환한다.")
    void parse() {
        //given
        String json = "[{\"market\":\"BTC\",\"trade_date_utc\":\"2025-06-03\",\"trade_time_utc\":\"10:00:00\",\"timestamp\":\"2025-06-03T10:00:00.000Z\",\"trade_price\":45000.0,\"trade_volume\":0.5,\"prev_closing_price\":44000.0,\"change_price\":1000.0,\"ask_bid\":\"ASK\",\"sequential_id\":123456}]";
        //when
        List<Ticks> ticks = tickParser.parseGson(json);
        //then
        assertEquals(1, ticks.size());
        Ticks tick = ticks.get(0);
        assertEquals("BTC", tick.getMarket());
        assertEquals(45000.0, tick.getTrade_price());
        assertEquals(0.5, tick.getTrade_volume());

    }

}