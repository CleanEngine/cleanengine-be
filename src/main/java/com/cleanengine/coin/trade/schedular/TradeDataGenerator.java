package com.cleanengine.coin.trade.schedular;

import com.cleanengine.coin.trade.service.TradeService;
import com.cleanengine.coin.trade.service.TradeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeDataGenerator {

    private final TradeServiceImpl tradeService;

//    @Scheduled(fixedDelay = 300) // 0.5초마다 실행
    public void generateTradeData() {
        tradeService.generateRandomTrade();
        log.info("Random trade generated successfully.");
    }
}