package com.cleanengine.coin.realitybot.domain;


import com.cleanengine.coin.realitybot.dto.Ticks;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.Getter;

import java.util.LinkedList;
import java.util.Queue;


@Getter
public class APIVWAPState {
    private final Queue<Ticks> ticksQueue = new LinkedList<>();
    private  VWAPCalculator calculator = new VWAPCalculator();
    private int maxQueueSize = 50;
    @WithSpan("api.request.call.ticker.apivwap")
    public void addTick(Ticks tick){
        if (ticksQueue.size() >= maxQueueSize) {
            //10개 이상이 되면 선착순으로 제거해나감
        Ticks removed = ticksQueue.poll();
        if (removed != null){
            calculator.removeTrade(removed.getTrade_price(), removed.getTrade_volume());
            }
        }
        //초기엔 들어온 갯수에 따라 증가시켜서 계산함
        ticksQueue.add(tick);
        calculator.recordTrade(tick.getTrade_price(),tick.getTrade_volume());
        //갯수 만큼 계산하기 때문에 정상 작동
//        calculator.getVWAP();
    }


    //n초마다 5회 주문 , api 체결 내역에서 10종목씩 비교
    @WithSpan("api.request.01.market.apivwap.getVolume")
    public double getAvgVolumePerOrder() {
        return calculator.getTotalVolume() / ticksQueue.size();
    }//todo 에러 인젝션으로 50일때와 5일때 복귀 속도 알아보기

    @WithSpan("api.request.01.market.apivwap.getPrice")
    public double getVWAP(){
        return calculator.getVWAP();
    }

    public int getTickSize() {
        return ticksQueue.size();
    }

}
