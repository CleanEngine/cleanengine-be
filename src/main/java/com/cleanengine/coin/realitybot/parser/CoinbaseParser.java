package com.cleanengine.coin.realitybot.parser;

import com.cleanengine.coin.realitybot.api.RateOfExchangeRefresher;
import com.cleanengine.coin.realitybot.dto.CoinbaseTicksResponse;
import com.cleanengine.coin.realitybot.dto.Ticks;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoinbaseParser implements ExchangesParser {
    private final RateOfExchangeRefresher rofExchanger;
    @Override
    public List<Ticks> parseJson(String json,String ticker) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        double usdToKrw = rofExchanger.getUsdToKrw();
        CoinbaseTicksResponse response = mapper.readValue(json, CoinbaseTicksResponse.class);
        return response.getTrades().stream()
                .map(tx -> Ticks.builder()
                        .market(tx.getProduct_id())
                        .timestamp(time(tx.getTime()))
                        .trade_price(Double.parseDouble(tx.getPrice())*usdToKrw)
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
}
