package com.cleanengine.coin.realitybot.service;


import com.cleanengine.coin.realitybot.dto.Ticks;

import java.util.LinkedList;
import java.util.Queue;


public class ApiVWAPService {
    private final Queue<Ticks> ticksQueue = new LinkedList<>();
    private double vwap;
    private double totalPriceVolume;
    private double totalVolume;

    public void addTick(Ticks tick){
        if (ticksQueue.size() >= 10) {
            //10개 이상이 되면 선착순으로 제거해나감
        Ticks removed = ticksQueue.poll();
        totalPriceVolume -= removed.getTrade_price() * removed.getTrade_volume();
        totalVolume -= removed.getTrade_volume();
    }
        //초기엔 들어온 갯수에 따라 증가시켜서 계산함
        ticksQueue.add(tick);
        totalPriceVolume += tick.getTrade_price() * tick.getTrade_volume();
        totalVolume += tick.getTrade_volume();
        //갯수 만큼 계산하기 때문에 정상 작동
        calculateVWAP();
    }

    private void calculateVWAP() {
        vwap = (totalVolume == 0) ? 0.0 : totalPriceVolume / totalVolume;
    }

    public double getVWAP() {
        return vwap;
    }

    public double getAvgVolumePerOrder() {
        return totalVolume / 30.0;
    }

    public int getTickSize() {
        return ticksQueue.size();
    }

}
