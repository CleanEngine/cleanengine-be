package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;

@Component
@RequiredArgsConstructor
public class VWAPerrorInJectionScheduler {

    private final TradeRepository tradeRepository;

    private boolean shouldInject = false;
    private boolean hasInjected = false;

    public void enableInjection() {
        if (hasInjected) return;  // 이미 실행한 적 있으면 무시
        this.shouldInject = true;
    }

    @Scheduled(fixedRate = 60000) // 혹은 따로 수동 호출도 가능
    public void injectFakeTrade() {
        if (!shouldInject || hasInjected) return;

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
        hasInjected = true;      // ✅ 한 번 실행했으니 플래그 설정
        shouldInject = false;

        System.out.println("🚨 혼동 Trade 1건 삽입 완료!");
    }
}
