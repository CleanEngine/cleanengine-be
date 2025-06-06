package com.cleanengine.coin.realitybot.parser;

import com.cleanengine.coin.realitybot.dto.OpeningPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OpeningPriceParserTest {
    private OpeningPriceParser openingPriceParser;

    @BeforeEach
    void setUp() {
        openingPriceParser = new OpeningPriceParser();
    }

    @Test
    @DisplayName("json이 주어지면 openingprice객체로 반환한다.")
    void testParseGson(){
        //given
        String json = "[{\"market\":\"BTC\", \"opening_price\":10000.0,\"trade_price\":15000.0}]";

        //when
        OpeningPrice openingPrice = openingPriceParser.parseGson(json);

        //then
        assertEquals("BTC", openingPrice.getMarket());
        assertEquals(10000.0,openingPrice.getOpening_price());
    }
}