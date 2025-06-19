package com.cleanengine.coin.realitybot.parser;

import com.cleanengine.coin.realitybot.dto.BinanceTicksResponse;
import com.cleanengine.coin.realitybot.dto.CoinbaseTicksResponse;
import com.cleanengine.coin.realitybot.dto.CoinoneTicksResponse;
import com.cleanengine.coin.realitybot.dto.Ticks;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Getter
public class TickParser {
    private final Gson gson = new Gson();
    @WithSpan("api.request.01.market.fallback.parse")
    public List<Ticks> parseGson(String json) {
        return gson.fromJson(json, new TypeToken<List<Ticks>>() {}.getType());
    }
    /*
    * TickService의 목적을 분류하여 api 로 받아온 값을 parsing 과 api vwap을 계산하는 로직으로 분류하였습니다.
    * 따라서 processVWAP과 CalculateVWAP은 제거되었으며
    * 해당 클래스는 TickParser로 명칭을 변경하였습니다.
    * */

    public List<Ticks> parseGsonByCoinbase(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        CoinbaseTicksResponse response = mapper.readValue(json, CoinbaseTicksResponse.class);
        return response.getTrades().stream()
                .map(tx -> Ticks.builder()
                        .market(tx.getProduct_id())
                        .timestamp(time(tx.getTime()))
                        .trade_price(Double.parseDouble(tx.getPrice()))
                        .trade_volume(Double.parseDouble(tx.getSize()))
                        .ask_bid(tx.getSide())
                        .sequential_id(Long.parseLong(tx.getTrade_id()))
                        .build()).toList();
    }
    public String time(Timestamp times){
        Instant instant = times.toInstant(); // 예: 2025-06-19 23:40:58.158
        long timestampMillis = instant.toEpochMilli(); // 밀리초로 변환
        String timestamp = String.valueOf(timestampMillis); // 문자열로 변환
        return timestamp;
    }

    public List<Ticks> parseGsonByBinance(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
//        JsonNode nodes = mapper.readTree(json);
//        List<Ticks> ticks = new ArrayList<>();
        List<BinanceTicksResponse> trades = mapper.readValue(json, new TypeReference<List<BinanceTicksResponse>>() {});

        List<Ticks> ticks = trades.stream().map(tx -> Ticks.builder()
                .timestamp(tx.getTime())
                .trade_price(tx.getPrice())
                .trade_volume(tx.getQty())
                .ask_bid(tx.isBuyerMaker()?"ASK":"BID")
                .sequential_id(tx.getId())
                .build())
                .toList();

        return ticks;
    }

    public List<Ticks> parseGsonByCoinone(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        CoinoneTicksResponse response = mapper.readValue(json, CoinoneTicksResponse.class);
        return response.getTransactions().stream()
                .map(tx -> Ticks.builder()
                        .market("KRW-" + response.getTarget_currency())
                        .timestamp(String.valueOf(tx.getTimestamp()))
                        .trade_price(Double.parseDouble(tx.getPrice()))
                        .trade_volume(Double.parseDouble(tx.getQty()))
                        .ask_bid(tx.isSellerMaker() ? "ASK" : "BID")
                        .sequential_id(Long.parseLong(tx.getId()))
                        .build())
                .toList();
    }

}
