package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.dto.Ticks;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class TickService implements TicketServiceInterface {
    private static final Gson gson = new Gson();//이거 왜 static으로? service에서 받아오면 되는데
    private final Queue<Ticks> ticksQueue;

    private double vwap;
    private double totalPriceVolume;
    private double totalVolume;

    public static List<Ticks> paraseGson(String json) {
        return gson.fromJson(json, new TypeToken<List<Ticks>>() {}.getType());
    }

    public void processVWAP(){ //vwap 구하기 실행
//        System.out.println("매서드 실행 시");
        if (ticksQueue.size()<10) {return;} //10개 이상일 경우 실행
        vwap = calculateVWAP(ticksQueue); //vwap 계산 실행

//        log.info("현재 VWAP: {}",vwap);
//        System.out.println("=== 현재 VWAP 가격 "+vwap);
    }

    @Override
    public double calculateVWAP(Queue<Ticks> ticksQueue) { //vwap (거래량 가중 평균가) 계산기
        //누적 거래 금액 (가격 * 거래량)
         totalPriceVolume = 0.0;
         // 누적 거래량을 저장
         totalVolume = 0.0;

         //queue 순회하여 각 틱당 가격과 거래량 누적 합산
        for (Ticks ticks : ticksQueue) {
            totalPriceVolume += ticks.getTrade_price() * ticks.getTrade_volume();//거래 금액 누적
            totalVolume += ticks.getTrade_volume();//거래량 누적

            /*//모니터링용 코드
            log.info("추가 된 가격 : {}",ticks.getTrade_price());
            log.info("추가 된 아이디 : {}",ticks.getSequential_id());
            */
        }
        //0 은 오류 방지
        //VWAP 반환
        return totalVolume == 0? 0.0: totalPriceVolume / totalVolume;

    }

}
