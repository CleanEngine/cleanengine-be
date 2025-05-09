package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.infra.TradeRepository;
import org.springframework.stereotype.Service;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public Trade saveTrade(Trade trade){

        return tradeRepository.save(trade);

    }

}
