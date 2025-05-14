package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.configuration.bootstrap.DBInitRunner;
import com.cleanengine.coin.order.application.OrderService;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderGenerateService {
    private final int[] orderLevels = {1,2,3};
    private final int unitPrice = 10; //TODO : 거래쌍 시세에 따른 호가 정책 개발 필요
    private final OrderQueueManagerService queueManager;
    private final PlatformVWAPService platformVWAPService;
    private final OrderService orderService;
    private final DBInitRunner dbInitRunner;
    private final TradeRepository tradeRepository;


        //TODO 비동기 처리로 전환 필요 + 시장 확인 후 주문량에 따른 오더 조절 필요
    public void generateOrder(double vwap, double avgVolum) {//기준 주문금액, 주문량 받기 (tick당 계산되어 들어옴)

        //todo 여기에 recordTrade를 불러 올 예정
        //불러와서 vwap을 넣어야 함
        List<Trade> trades = tradeRepository.findTop10ByTickerOrderByTradeTimeDesc("TRUMP");

        // Platform 기반 가격 , 최초 0.0원
//        double platformVWAP = platformVWAPService.getPlatformVWAP(); //order를 넣고 체결한 trade queue 값을 기준으로 계산
        double platformVWAP = platformVWAPService.calculateVWAPbyTrades(trades);
        //todo 0513 이거 체결량 그만큼 채워지는 거 아니면 작동 안되도록 전환 필요
        if(platformVWAP == 0.0){ //최초 실행 시 vwap 계산
            platformVWAP = generateVirtualVWAP(vwap); //0.1% 보정값 랜덤 생성
        }

        //근데 이거 platforVWAP 이 값이 있을 경우 작동해야 함
        double trendLineRate = (platformVWAP - vwap)/ vwap;
        boolean isWithinRange = Math.abs(trendLineRate) <= 0.01;

        //VWAP 선택기 = 최초1회 API, 이후 Tick기반 todo 제거 대상
//        double virtualVWAP = virtualMarketService.switcherVWAP(tickService.getTicksQueue(), platformVWAP);


        //TODO 체결 제작 후 VirtualVWAP이 아닌 PlatformVWAP 로 전환필요 ✔

        for(int level : orderLevels) { //1주문당 3회 매수매도 처리
//            List<Trade> trades = tradeRepository.findAll(); //10개 범위? 최근데이터
            double sellPrice;
            double buyPrice;
            double priceOffset = unitPrice * level; //3단계 호가 각각 처리

            //1단계 밀집 주문
            if (level == 1){ //1level일 경우 주문이 겹치도록 설정
                double basePrice = normalizeToUnit(platformVWAP); //기준 가격 (호가 단위 정규화)
                double randomOffset =  level1TradeMaker(platformVWAP,0.005);//랜덤 오차 부여 (가격 조정용)
                //체결을 위해 매수가 올리고, 매도가 내리는 계산 적용
                sellPrice = normalizeToUnit(basePrice - randomOffset);
                buyPrice = normalizeToUnit(basePrice + randomOffset);
            }
            //2~3 단계 : orderbook 단위 주문
            else {
                double randomOffset =  level1TradeMaker(platformVWAP,0.01);
                //체결 확률 증가용 코드
                sellPrice = normalizeToUnit(platformVWAP + priceOffset - randomOffset);
                buyPrice = normalizeToUnit(platformVWAP - priceOffset + randomOffset);
                //안정적인 스프레드 유지
//                sellPrice = normalizeToUnit(platformVWAP + priceOffset);
//                buyPrice = normalizeToUnit(platformVWAP - priceOffset);
            }

            //주문 실행
//            sellPrice = normalizeToUnit(virtualVWAP + priceOffset);//todo : 제거 대상
            double sellVolume = getRandomVolum(avgVolum);
            double buyVolume = getRandomVolum(avgVolum);

            if (!(platformVWAP==0)){
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
                    sellPrice = normalizeToUnit(sellPrice - (vwap * correctionRate)); // 매도 더 싸게 → 체결 유도
                    buyPrice = normalizeToUnit(buyPrice + (vwap * correctionRate)); // 매수 더 비싸게 → 체결 유도
                } else if (trendLineRate > 0.01) { // platformVWAP이 너무 높음
                    sellPrice = normalizeToUnit(sellPrice + (vwap * correctionRate)); // 매도 더 비싸게
                    buyPrice = normalizeToUnit(buyPrice - (vwap * correctionRate)); // 매수 더 싸게
                    //platform vwap -> vwap으로 변환
                }
                try{
//                추세선 벗어나면 작동하는 주문
                orderService.createOrderWithBot("TRUMP",false, sellVolume,sellPrice);
                orderService.createOrderWithBot("TRUMP",true, buyVolume,buyPrice);
//
                } catch (DomainValidationException e) {
                    log.warn("매수금 잔량 부족 {}",e.getMessage());
                    try {
                        dbInitRunner.run();
                        //주문 재시도
                        orderService.createOrderWithBot("TRUMP",false, sellVolume,sellPrice);
                        orderService.createOrderWithBot("TRUMP",true, buyVolume,buyPrice);
                    } catch (Exception e1) {
                        log.error("init 초기화 중 예외 발생",e1);
                    }
                }

                queueManager.addSellOrder(sellPrice, sellVolume);
                queueManager.addBuyOrder(buyPrice, buyVolume); //Queue 추가
            }

            try{
            orderService.createOrderWithBot("TRUMP",false, sellVolume,sellPrice);
            orderService.createOrderWithBot("TRUMP",true, buyVolume,buyPrice);
            } catch (DomainValidationException e) {
                log.warn("매수금 잔량 부족 {}",e.getMessage());
                try {
                    dbInitRunner.run();

                    //주문 재시도
                    orderService.createOrderWithBot("TRUMP",false, sellVolume,sellPrice);
                    orderService.createOrderWithBot("TRUMP",true, buyVolume,buyPrice);
                } catch (Exception e1) {
                    log.error("init 초기화 중 예외 발생",e1);
                }
            }

            //가상 주문 체결
            queueManager.addSellOrder(sellPrice, sellVolume);
            queueManager.addBuyOrder(buyPrice, buyVolume); //Queue 추가
            try {
                TimeUnit.MICROSECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //모니터링용
            System.out.println("sellPrice = " + sellPrice);
            System.out.println("sellVolume = " + sellVolume);
//            buyPrice = normalizeToUnit(virtualVWAP - priceOffset);//todo : 제거 대상
            //모니터링용
            System.out.println("buyPrice = " + buyPrice);
            System.out.println("buyVolume = " + buyVolume);

            System.out.println("====================================");
            System.out.println("현재 시장 vwap "+vwap+"  현재 플랫폼 vwap"+platformVWAP);
            System.out.println("====================================");
        }

    }

    //todo VirtualMarketService 여기에도 있는데 공통화 필요? , 계수는 조금 다른긴함 -> vms 제거 대상
    private double generateVirtualVWAP(double apiVWAP) {//가상 vwap 계산 , 최초 1회 사용
        double maxDeviationaRate = 0.001; //보정값 0.1%만
        double deviation = (Math.random() * 2 - 1)* maxDeviationaRate; //편차 계산
        return apiVWAP * (1+deviation); // +=deviation 난수 생성 후 계산 (범위는 -1~+1)
    }

    //==================================order 정규화용 ============================================

    private double level1TradeMaker(double apiVWAP, double maxRate){
        double percent = (Math.random() * 2-1)*maxRate;
        return apiVWAP * percent;
    }

    private int normalizeToUnit(double price){ //호가단위로 변환
        return (int)(Math.round(price / unitPrice)) * unitPrice;
//        return (int) (price / unitPrice) * unitPrice;
    }
    private double getRandomVolum(double avgVolum){ //볼륨 랜덤 입력
        double rawVolume = avgVolum * (0.5+Math.random());
        return Math.round(rawVolume * 1000.0)/1000.0;
    }
}
