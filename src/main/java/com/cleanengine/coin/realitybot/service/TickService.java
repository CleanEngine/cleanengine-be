package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.dto.Ticks;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
@RequiredArgsConstructor
@Slf4j
public class TickService implements TicketServiceInterface {
    private static final Gson gson = new Gson();//이거 왜 static으로? service에서 받아오면 되는데
    private final Queue<Ticks> ticksQueue;

//    public TickService(Queue<Ticks> ticksQueue) { //어노테이션 추가해서 지움
//        this.ticksQueue = ticksQueue;
//    }

    public static List<Ticks> paraseGson(String json) {
        return gson.fromJson(json, new TypeToken<List<Ticks>>() {}.getType());
    }
    public void processVWAP(){
        System.out.println("매서드 실행 시");
        if (ticksQueue.size()<10) {return;}
        double vwap = calculateVWAP(ticksQueue);

        log.info("현재 VWAP: {}",vwap);
    }

    @Override
    public double calculateVWAP(Queue<Ticks> ticksQueue) {
        double totalPriceVolume = 0.0;
        double totalVolume = 0.0;

        for (Ticks ticks : ticksQueue) {
            totalPriceVolume += ticks.getTrade_price() * ticks.getTrade_volume();
            totalVolume += ticks.getTrade_volume();
            log.info("추가 된 가격 : {}",ticks.getTrade_price());
            log.info("추가 된 아이디 : {}",ticks.getSequential_id());
        }

        return totalVolume == 0? 0.0: totalPriceVolume / totalVolume;

    }

}
