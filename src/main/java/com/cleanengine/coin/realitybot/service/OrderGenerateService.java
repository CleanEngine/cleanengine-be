package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.order.application.OrderService;
import com.cleanengine.coin.order.external.adapter.account.AccountExternalRepository;
import com.cleanengine.coin.order.external.adapter.wallet.WalletExternalRepository;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderGenerateService {
    private final int[] orderLevels = {1,2,3};
    private final int unitPrice = 10; //TODO : 거래쌍 시세에 따른 호가 정책 개발 필요
    private final PlatformVWAPService platformVWAPService;
    private final OrderService orderService;
    private final TradeRepository tradeRepository;
    private final VWAPerrorInJectionScheduler vwaPerrorInJectionScheduler;
    private final WalletExternalRepository walletExternalRepository;
    private final AccountExternalRepository accountExternalRepository;
    private String ticker;


    public void generateOrder(String ticker, double apiVWAP, double avgVolume) {//기준 주문금액, 주문량 받기 (tick당 계산되어 들어옴)
        this.ticker = ticker;

        //최근 체결 내역 가져오기
        List<Trade> trades = tradeRepository.findTop10ByTickerOrderByTradeTimeDesc(ticker);

        // Platform 기반 가격 생성 (10개 이하, 10개 이상에 따른 가격 생성)
        double platformVWAP = platformVWAPService.calculateVWAPbyTrades(ticker,trades,apiVWAP);

        //편차 계산 (vwap 기준)
        double trendLineRate = (platformVWAP - apiVWAP)/ apiVWAP;

        //편차가 +-5% 이상 발생하면 true 반환
        boolean isWithinRange = Math.abs(trendLineRate) <= 0.005; //TODO 호가 단위에 따른 편차 보정 필요
        for(int level : orderLevels) { //1주문당 3회 매수매도 처리
            double priceOffset = unitPrice * level; //호가 단위만큼 단계별 offset 설정
            //randomoffset는 1단계 밀집 주문을 위해 offset 편차가 많이 안나도록 동적으로 max를 제한함
            double randomOffset =  Math.abs(level1TradeMaker(platformVWAP,getDynamicMaxRate(trendLineRate)));
            double deviation = Math.abs(trendLineRate); //편차 구하기
            double sellPrice;
            double buyPrice;

            //1단계 밀집 주문
            if (level == 1){ //1level일 경우 주문이 겹치도록 설정
                double basePrice = normalizeToUnit(platformVWAP); //기준 가격 (호가 단위 정규화)
                //체결을 위해 매수가 올리고, 매도가 내리는 계산 적용
                sellPrice = normalizeToUnit(basePrice - randomOffset);
                buyPrice = normalizeToUnit(basePrice + randomOffset);
            }
            //2~3 단계 : orderbook 단위 주문
            else {
                 randomOffset =  level1TradeMaker(platformVWAP,0.01);
                //체결 확률 증가용 코드
                sellPrice = normalizeToUnit(platformVWAP + priceOffset - randomOffset);
                buyPrice = normalizeToUnit(platformVWAP - priceOffset + randomOffset);
                //안정적인 스프레드 유지
//                sellPrice = normalizeToUnit(platformVWAP + priceOffset);
//                buyPrice = normalizeToUnit(platformVWAP - priceOffset);
            }

            //주문 실행
            double sellVolume = getRandomVolum(avgVolume);
            double buyVolume = getRandomVolum(avgVolume);

            if (platformVWAP != 0){
                if (isWithinRange){
                    if (trendLineRate > 0){
                        sellVolume *=1.5;
                        buyVolume *= 0.7;
                    } else {
                        sellVolume *=0.7;
                        buyVolume *= 1.5;
                    }
                }
                double correctionRate = 0.1;
                if (trendLineRate < -0.01) { // platformVWAP이 너무 낮음
                    sellPrice = normalizeToUnit(sellPrice + (apiVWAP * correctionRate)); // 매도 비싸게
                    buyPrice = normalizeToUnit(buyPrice + (apiVWAP * correctionRate)); // 매수 비싸게
                } else if (trendLineRate > 0.01) { // platformVWAP이 너무 높음
                    sellPrice = normalizeToUnit(sellPrice - (apiVWAP * correctionRate)); // 매도 싸게
                    buyPrice = normalizeToUnit(buyPrice - (apiVWAP * correctionRate)); // 매수 싸게
                    //platform vwap -> vwap으로 변환
                }


                // 편차에 따라 강도 조절
                if (deviation > 0.01) {
                    double power = trendLineRate * 100; // 3% → 3
                    if (trendLineRate < 0) {
                        buyVolume *= 1.0 + Math.abs(power) * 0.5; // 3% → 2.5배
                        sellVolume *= 1.0 + Math.abs(power) * 0.5;
                        buyPrice = normalizeToUnit(apiVWAP * (1 + 0.002 * power)); // +0.6%
                        sellPrice = normalizeToUnit(apiVWAP * (1 + 0.002 * power)); // +0.6%
                    } else {
                        buyVolume *= 1.0 + Math.abs(power) * 0.5; // 3% → 2.5배
                        buyPrice = normalizeToUnit(apiVWAP * (1 - 0.002 * power)); // -0.6%
                        sellVolume *= 1.0 + Math.abs(power) * 0.5;
                        sellPrice = normalizeToUnit(apiVWAP * (1 - 0.002 * power)); // -0.6%
                    }
                }
                createOrderWithFallback(ticker,false, sellVolume,sellPrice);
                createOrderWithFallback(ticker,true, buyVolume,buyPrice);

//                queueManager.addSellOrder(sellPrice, sellVolume);
//                queueManager.addBuyOrder(buyPrice, buyVolume); //Queue 추가
            } else {

                //스위치 시켜야 할까?
                createOrderWithFallback(ticker,false, sellVolume,sellPrice);
                createOrderWithFallback(ticker,true, buyVolume,buyPrice);

//                queueManager.addSellOrder(sellPrice, sellVolume);
//                queueManager.addBuyOrder(buyPrice, buyVolume);

            }



            try {
                TimeUnit.MICROSECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//            vwaPerrorInJectionScheduler.enableInjection(); //에러 발생기 비활성화

            /*//모니터링용
            System.out.println("sellPrice = " + sellPrice);
            System.out.println("sellVolume = " + sellVolume);
            //모니터링용
            System.out.println("buyPrice = " + buyPrice);
            System.out.println("buyVolume = " + buyVolume);

            System.out.println("====================================");
            DecimalFormat df = new DecimalFormat("#,##0.00");
            System.out.println(ticker+"의 현재 시장 vwap :"+df.format(apiVWAP)+" | 현재 플랫폼 vwap :"+df.format(platformVWAP));*/

        }
/*        System.out.println("📦"+ticker+" [체결 기록 Top 10]");
        trades.forEach(t ->
                System.out.printf("🕒 %s | 가격: %.0f | 수량: %.8f | 매수: #%d ↔ 매도: #%d%n",
                        t.getTradeTime(), t.getPrice(), t.getSize(), t.getBuyUserId(), t.getSellUserId())
        );*/
    }

    private void createOrderWithFallback(String ticker,boolean isBuy, double volume, double price ) {
        if (volume <= 0 || price <= 0){
            log.error("잘못된 주문이 발생 [종목 : {}] ,[isBuy : {}] ,[금액 : {}] ,[수량 : {}] 주문은 생성 취소",ticker,isBuy,
                    new DecimalFormat("#,###.########").format(price),
                    new DecimalFormat("#,###.########").format(volume));
            return;
        } 
        
        try {
            orderService.createOrderWithBot(ticker, isBuy, volume, price);
        } catch (DomainValidationException e) {
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
        this.ticker = ticker;
        Wallet wallet = walletExternalRepository.findWalletBy(SELL_ORDER_BOT_ID,ticker).get();
        wallet.setSize(500_000_000.0);
        Wallet wallet2 = walletExternalRepository.findWalletBy(BUY_ORDER_BOT_ID,ticker).get();
        wallet2.setSize(0.0);
        walletExternalRepository.save(wallet);
        walletExternalRepository.save(wallet2);

        Account account = accountExternalRepository.findByUserId(SELL_ORDER_BOT_ID).get();
        account.setCash(0.0);
        Account account2 = accountExternalRepository.findByUserId(BUY_ORDER_BOT_ID).get();
        account2.setCash(500_000_000.0);
        accountExternalRepository.save(account);
        accountExternalRepository.save(account2);
    }

    //==================================order 정규화용 ============================================

    private double level1TradeMaker(double platformVWAP, double maxRate){
        //시장가에 해당하는 호가는 거래 체결 강하게 하기 위함
        double percent = (Math.random() * 2-1)*maxRate;
        return platformVWAP * percent;
    }

    private double getDynamicMaxRate(double trendLineRate) {
        // 편차가 벌어지면 벌어질수록 보정폭 확대
        // 5% = 2.51의 가중치
        // 11% = 5.51의 가중치
        return 0.01 + Math.abs(trendLineRate) * 0.5;
    }

    private int normalizeToUnit(double price){ //호가단위로 변환
        return (int)(Math.round(price / unitPrice)) * unitPrice;
    }
    private double getRandomVolum(double avgVolum){ //볼륨 랜덤 입력
        double rawVolume = avgVolum * (0.5+Math.random());
        //호가 단위에 따라 0원이 발생 가능성
        double resultVolume = Math.round(rawVolume * 1000.0)/1000.0;
        if(resultVolume <= 0){
            //Volume이 0이하일 경우 재 계산
            resultVolume = Math.round(rawVolume * 10000000.0)/10000000.0;
        }
        return resultVolume;
    }
}
