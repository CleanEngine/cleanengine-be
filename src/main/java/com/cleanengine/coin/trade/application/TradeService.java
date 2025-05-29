package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import org.springframework.stereotype.Service;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public Trade save(Trade trade) {
        return tradeRepository.save(trade);
    }

}
