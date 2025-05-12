package com.cleanengine.coin.realitybot.service;

import org.springframework.stereotype.Service;

@Service
public class OrderGenerateService {
    private final int[] orderLevels = {1,2,3};
    private final int unitPrice = 100; //TODO : 거래쌍 시세에 따른 호가 정책 개발 필요
    private OrderQueueManagerService queueManager;
    private VirtualMarketService virtualMarketService;
    private PlatformVWAPService platformVWAPService;
    private TickService tickService;

//    private final TradeRepository tradeRepository;

    public OrderGenerateService(OrderQueueManagerService queueManager, VirtualMarketService virtualMarketService,
                                PlatformVWAPService platformVWAPService, TickService tickService/*,TradeRepository tradeRepository*/) {
        this.queueManager = queueManager;
        this.virtualMarketService = virtualMarketService;
        this.platformVWAPService = platformVWAPService;
        this.tickService = tickService;
//        this.tradeRepository = tradeRepository;
    }

        //TODO 비동기 처리로 전환 필요 + 시장 확인 후 주문량에 따른 오더 조절 필요
    public void generateOrder(double vwap, double avgVolum) {//기준 주문금액, 주문량 받기 (tick당 계산되어 들어옴)

        // Platform 기반 가격 , 최초 0.0원
        double platformVWAP = platformVWAPService.getPlatformVWAP(); //order를 넣고 체결한 trade queue 값을 기준으로 계산
        if(platformVWAP == 0.0){ //최초 실행 시 vwap 계산
            platformVWAP = generateVirtualVWAP(vwap); //0.1% 보정값 랜덤 생성
        }

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
                //체결 확률 증가용 코드
//                sellPrice = normalizeToUnit(virtualVWAP + priceOffset - randomOffset);
//                buyPrice = normalizeToUnit(virtualVWAP - priceOffset + randomOffset);
                //안정적인 스프레드 유지
                sellPrice = normalizeToUnit(platformVWAP + priceOffset);
                buyPrice = normalizeToUnit(platformVWAP - priceOffset);
            }

            //주문 실행
//            sellPrice = normalizeToUnit(virtualVWAP + priceOffset);//todo : 제거 대상
            double sellVolume = getRandomVolum(avgVolum);
            queueManager.addSellOrder(sellPrice, sellVolume);
            /*//모니터링용
            System.out.println("sellPrice = " + sellPrice);
            System.out.println("sellVolume = " + sellVolume);
*/

//            buyPrice = normalizeToUnit(virtualVWAP - priceOffset);//todo : 제거 대상
            double buyVolume = getRandomVolum(avgVolum);
            queueManager.addBuyOrder(buyPrice, buyVolume); //Queue 추가
            /*//모니터링용
            System.out.println("buyPrice = " + buyPrice);
            System.out.println("buyVolume = " + buyVolume);
*/
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
//        return ((int)Math.round(price * unitPrice)) * unitPrice;
        return (int) (price / unitPrice) * unitPrice;
    }
    private double getRandomVolum(double avgVolum){ //볼륨 랜덤 입력
        double rawVolume = avgVolum * (0.5+Math.random());
        return Math.round(rawVolume * 1000.0)/1000.0;
    }
}
