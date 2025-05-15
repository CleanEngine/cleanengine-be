package com.cleanengine.coin.orderbook.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookInfoSerializationTest {

    public static ObjectMapper objectMapper;

    @BeforeAll
    static void initStatic(){
        objectMapper = new ObjectMapper();
    }

    @Test
    void eachOrderBookHasOnePrice_serializeIt_resultEqualsAsExpected() throws JsonProcessingException {
        OrderBookInfo orderBookInfo = new OrderBookInfo("BTC",
                List.of(new OrderBookUnitInfo(1.0, 1.0)),
                List.of(new OrderBookUnitInfo( 2.0, 2.0)));

        String json = objectMapper.writeValueAsString(orderBookInfo);
        System.out.println(json);
        assertEquals("{\"ticker\":\"BTC\",\"buyOrderBookUnits\":[{\"price\":1.0,\"size\":1.0}],\"sellOrderBookUnits\":[{\"price\":2.0,\"size\":2.0}]}", json);
    }

    @Test
    void oneOfOrderBookIsEmpty_serializeIt_resultEqualsAsExpected() throws JsonProcessingException {
        OrderBookInfo orderBookInfo = new OrderBookInfo("BTC",
                List.of(new OrderBookUnitInfo(1.0, 1.0)),
                List.of());

        String json = objectMapper.writeValueAsString(orderBookInfo);
        System.out.println(json);
        assertEquals("{\"ticker\":\"BTC\",\"buyOrderBookUnits\":[{\"price\":1.0,\"size\":1.0}],\"sellOrderBookUnits\":[]}", json);
    }
}