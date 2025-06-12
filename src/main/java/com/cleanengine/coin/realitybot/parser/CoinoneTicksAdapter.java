package com.cleanengine.coin.realitybot.parser;

import com.cleanengine.coin.realitybot.dto.CoinoneTicksResponse;
import com.cleanengine.coin.realitybot.dto.Ticks;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoinoneTicksAdapter {

    private final TickParser tickParser;

    public List<Ticks> convertToTicks(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        CoinoneTicksResponse response = mapper.readValue(json, CoinoneTicksResponse.class);
        return response.getTransactions().stream()
                .map(tx -> Ticks.builder()
                        .market("KRW-" + response.getTarget_currency())
                        .timestamp(String.valueOf(tx.getTimestamp()))
                        .trade_price(Float.parseFloat(tx.getPrice()))
                        .trade_volume(Double.parseDouble(tx.getQty()))
                        .ask_bid(tx.isSellerMaker() ? "ASK" : "BID")
                        .sequential_id(Long.parseLong(tx.getId()))
                        .build())
                .toList();
    }
}
