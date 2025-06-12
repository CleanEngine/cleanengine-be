package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;

@Component
public class VWAPErrorInjector {
    private final TradeRepository tradeRepository;

    public VWAPErrorInjector(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public void injectErrorTrade(){
        Trade fakeTrade = new Trade();
        fakeTrade.setTicker("TRUMP");
        fakeTrade.setBuyUserId(BUY_ORDER_BOT_ID);  // 테스트용 유저 ID
        fakeTrade.setSellUserId(SELL_ORDER_BOT_ID); // 테스트용 유저 ID
        fakeTrade.setPrice(25000.0);   // 말도 안되는 고가 (예: 시장 평균이 19,000일 때)
//        fakeTrade.setPrice(18900.0);   // 말도 안되는 고가 (예: 시장 평균이 19,000일 때)
        fakeTrade.setSize(300.0);     // 대량 체결
//        fakeTrade.setSize(100.0);     // 대량 체결
        fakeTrade.setTradeTime(LocalDateTime.now());

        tradeRepository.save(fakeTrade);
    }
}
