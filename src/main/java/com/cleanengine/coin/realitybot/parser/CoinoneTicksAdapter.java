package com.cleanengine.coin.realitybot.parser;

import com.cleanengine.coin.realitybot.dto.CoinoneTicksResponse;
import com.cleanengine.coin.realitybot.dto.Ticks;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoinoneTicksAdapter {
    public List<Ticks> convertToTicks(CoinoneTicksResponse response, String market){
        return response.getTrades().stream()
                .map(tx -> Ticks.builder()
                        .market(market)
                        .timestamp(String.valueOf(tx.getTimestamp()))
                        .trade_price(Float.parseFloat(tx.getPrice()))
                        .trade_volume(Double.parseDouble(tx.getQty()))
                        .ask_bid(tx.is_seller_maker() ? "ASK" : "BID")
                        .sequential_id(Long.parseLong(tx.getId()))
                        .build())
                .toList();
    }
}
