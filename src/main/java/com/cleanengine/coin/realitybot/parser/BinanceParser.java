package com.cleanengine.coin.realitybot.parser;

import com.cleanengine.coin.realitybot.api.RateOfExchangeRefresher;
import com.cleanengine.coin.realitybot.dto.BinanceTicksResponse;
import com.cleanengine.coin.realitybot.dto.Ticks;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceParser implements ExchangesParser {
    private final RateOfExchangeRefresher rofExchanger;

    @Override
    public List<Ticks> parseJson(String json, String ticker) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
//        JsonNode nodes = mapper.readTree(json);
//        List<Ticks> ticks = new ArrayList<>();
        List<BinanceTicksResponse> trades = mapper.readValue(json, new TypeReference<List<BinanceTicksResponse>>() {});

        List<Ticks> ticks = trades.stream().map(tx -> Ticks.builder()
                        .market(ticker)
                        .timestamp(tx.getTime())
                        .trade_price(tx.getPrice()*getKrw(ticker))
                        .trade_volume(tx.getQty())
                        .ask_bid(tx.isBuyerMaker()?"ASK":"BID")
                        .sequential_id(tx.getId())
                        .build())
                .toList();
        return ticks;
    }

    public double getKrw(String ticker){
        double krw = rofExchanger.getUsdToKrw();
        if (ticker.equalsIgnoreCase("USDT")){
            krw = rofExchanger.getTryToKrw();
        }
        return krw;
    }
}
