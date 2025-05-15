package com.cleanengine.coin.realitybot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class VirtualMarketService {//이거 안씀이젬
    private final Random random = new Random();
    private final TickService tickService;

    //1회용 으로 전환
    private boolean initialized = false;
    private double initialVWAP;

    public VirtualMarketService(TickService tickService) {
        this.tickService = tickService;
    }

//    public double switcherVWAP(Queue<Ticks> ticksQueue, double platformVWAP){
//        if (!initialized || platformVWAP == 0.0) {
//            initialVWAP = getVirtualMarketPrice(ticksQueue);
//            initialized = true;
//            log.info("초기 VWAP 생성: {}", initialVWAP);
//            return initialVWAP;
//        } else {
//            log.info("초기 VWAP 생성: {}", initialVWAP);
//            log.info("vwap 생성 : {}",platformVWAP);
//            return platformVWAP;
//        }
//    }

//    public double getVirtualMarketPrice(Queue<Ticks> ticksQueue) { //10개로 제한 된 ticks를 받음
//        double realVWAP = tickService.calculateVWAP(ticksQueue);//다시 평균가 계산 : 또 계산할 필요가 있을까?
//        double adjustment = getRandomAdjustment(realVWAP); //랜덤 조정 : +-0.5% 범위 내 랜덤 값 부여
//
//        double virtualPrice = realVWAP + adjustment; // 최종 가격
//
//        /*//모니터링 (가상 = trade 체결금액에서 vwap)만 구하면 됨
//        log.info("현실 VWAP : {}, 보정치 : {}, 가상 VWAP : {}", realVWAP, adjustment, virtualPrice);
//        */
//
//        return virtualPrice;
//    }

//    private double getRandomAdjustment(double realVWAP) { //가상 VWAP 제작용 랜덤 값 부여
//        double maxDeviationRate = 0.005; //(0.5%는 보정값)
//        double deviation = realVWAP * maxDeviationRate;//편차 계산
//
//        return (random.nextDouble()*2-1)* deviation; // +=deviation 난수 생성 후 계산 (범위는 -1~+1)
//    }
}
