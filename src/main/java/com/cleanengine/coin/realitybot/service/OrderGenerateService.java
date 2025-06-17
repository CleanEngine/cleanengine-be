package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.order.adapter.out.persistentce.account.OrderAccountRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.wallet.OrderWalletRepository;
import com.cleanengine.coin.order.application.OrderService;
import com.cleanengine.coin.realitybot.api.UnitPriceRefresher;
import com.cleanengine.coin.realitybot.domain.VWAPMetricsRecorder;
import com.cleanengine.coin.realitybot.vo.DeviationPricePolicy;
import com.cleanengine.coin.realitybot.vo.OrderPricePolicy;
import com.cleanengine.coin.realitybot.vo.OrderVolumePolicy;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;

@Slf4j
@Service
@Order(5)
@RequiredArgsConstructor
public class OrderGenerateService {
    private final VWAPMetricsRecorder VWAPMetricsRecorder;
    @Value("${bot-handler.order-level}")
    private int[] orderLevels; //체결 강도
    private double unitPrice = 0; //TODO : 거래쌍 시세에 따른 호가 정책 개발 필요
    private final UnitPriceRefresher unitPriceRefresher;
    private final PlatformVWAPService platformVWAPService;
    private final OrderService orderService;
    private final OrderPricePolicy orderPricePolicy;
    private final DeviationPricePolicy deviationPricePolicy;
    private final OrderVolumePolicy orderVolumePolicy;
    private final OrderWalletRepository orderWalletRepository;
    private final OrderAccountRepository accountExternalRepository;

    private final VWAPMetricsRecorder recorder;


    @WithSpan("api.request.02.order")
    public void generateOrder(String ticker, double apiVWAP, double avgVolume) {//기준 주문금액, 주문량 받기 (tick당 계산되어 들어옴)

        //호가 정책 적용
        this.unitPrice = unitPriceRefresher.getUnitPriceByTicker(ticker);

        // Platform 기반 가격 생성 (10개 이하, 10개 이상에 따른 가격 생성)
        double platformVWAP = platformVWAPService.calculateVWAPbyTrades(ticker,apiVWAP);
        recorder.recordPlatformVwap(ticker,platformVWAP);
        recorder.recordErrorRateVwap(ticker, apiVWAP, platformVWAP);
        //편차 계산 (vwap 기준)
        double trendLineRate = (platformVWAP - apiVWAP)/ apiVWAP;
        for(int level : orderLevels) { //1주문당 3회 매수매도 처리
            OrderPricePolicy.OrderPrice basePrice = orderPricePolicy.calculatePrice(level,platformVWAP,unitPrice,trendLineRate);
            DeviationPricePolicy.AdjustPrice adjustPrice = deviationPricePolicy.adjust(
                    basePrice.sell(), basePrice.buy(), trendLineRate, apiVWAP, unitPrice);

            double sellVolume = orderVolumePolicy.calculateVolume(avgVolume,trendLineRate,false);
            double buyVolume = orderVolumePolicy.calculateVolume(avgVolume,trendLineRate,true);
            double sellPrice = adjustPrice.sell();
            double buyPrice = adjustPrice.buy();


                createOrderWithFallback(ticker,false, sellVolume, sellPrice);
                createOrderWithFallback(ticker,true, buyVolume, buyPrice);

/*            DecimalFormat df = new DecimalFormat("#,##0.00");
            DecimalFormat dfv = new DecimalFormat("#,###.########");
            //모니터링용
            System.out.println("sellPrice = " + df.format(sellPrice));
            System.out.println("sellVolume = " + dfv.format(sellVolume));
            //모니터링용
            System.out.println("buyPrice = " + df.format(buyPrice));
            System.out.println("buyVolume = " + dfv.format(buyVolume));

            System.out.println("====================================");
            System.out.println(ticker+"의 현재 시장 vwap :"+df.format(apiVWAP)+" | 현재 플랫폼 vwap :"+df.format(platformVWAP));*/
        }
        /*System.out.println("📦"+ticker+" [체결 기록 Top 10]");
        trades.forEach(t ->
                System.out.printf("🕒 %s | 가격: %.0f | 수량: %.8f | 매수: #%d ↔ 매도: #%d%n",
                        t.getTradeTime(), t.getPrice(), t.getSize(), t.getBuyUserId(), t.getSellUserId())
        );*/
    }
    @WithSpan("api.request.03.create")
    private void createOrderWithFallback(String ticker,boolean isBuy, double volume, double price ) throws IllegalArgumentException {
        if (volume <= 0 || price <= 0){
            log.error("잘못된 주문이 발생 [종목 : {}] ,[isBuy : {}] ,[금액 : {}] ,[수량 : {}] 주문은 생성 취소",ticker,isBuy,
                    new DecimalFormat("#,###.########").format(price),
                    new DecimalFormat("#,###.########").format(volume));
            return;
        }
        recorder.recordPrice(ticker,isBuy,price);
        try {
            orderService.createOrderWithBot(ticker, isBuy, volume, price);
        } catch (IllegalArgumentException e) {
            log.debug("잔량 부족: {}", e.getMessage());
            try {
                resetBot(ticker);
                orderService.createOrderWithBot(ticker, isBuy, volume, price);
            } catch (Exception e1) {
                log.error("주문 재시도 실패", e1);
            }
        }
    }

    protected void resetBot(String ticker){
        Wallet wallet = orderWalletRepository.findWalletBy(SELL_ORDER_BOT_ID,ticker).get();
        wallet.setSize(500_000_000.0);
        Wallet wallet2 = orderWalletRepository.findWalletBy(BUY_ORDER_BOT_ID,ticker).get();
        wallet2.setSize(0.0);
        orderWalletRepository.save(wallet);
        orderWalletRepository.save(wallet2);

        Account account = accountExternalRepository.findByUserId(SELL_ORDER_BOT_ID).get();
        account.setCash(0.0);
        Account account2 = accountExternalRepository.findByUserId(BUY_ORDER_BOT_ID).get();
        account2.setCash(500_000_000.0);
        accountExternalRepository.save(account);
        accountExternalRepository.save(account2);
    }

}
