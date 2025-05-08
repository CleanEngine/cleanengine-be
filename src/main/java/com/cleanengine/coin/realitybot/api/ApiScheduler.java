package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.realitybot.dto.Ticks;
import com.cleanengine.coin.realitybot.service.TickService;
import com.cleanengine.coin.realitybot.service.VirtualMarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiScheduler implements DisposableBean {

    private final BithumbAPIClient bithumbAPIClient;
    private final TickService tickService;
    private final VirtualMarketService virtualMarketService;
    private long lastMaxSequentialId = 1L;
//    private final Queue<Ticks> ticksQueue = new LinkedList<>();
    private final Queue<Ticks> ticksQueue;

    @Scheduled(fixedRate = 5000)
    public void MarketDataRequest(){
        String rawJson = bithumbAPIClient.get();
        List<Ticks> gson = TickService.paraseGson(rawJson);
//        Queue<Ticks> gsonQueue = (Queue<Ticks>) TickService.paraseGson(rawJson);
//        for(Ticks ticks : gson){//1차
        for (int i = gson.size()-1; i >=0 ; i--) {//2차 : 10
            Ticks ticks = gson.get(i);
//            System.out.println("fori = " + gson.size());
            if (ticks.getSequential_id() > lastMaxSequentialId){
                //----------------------
                if (ticksQueue.size()>=10){ //갯수를 20개 까지만으로 제한
                    ticksQueue.poll();  //queue에서 빼기, 추세를 구현하기만 하면 필요가 있을까? -> 없음
                }//멀티 쓰레드환경이 아니므로 concurrentlinkedqueue는 나중에 구현
                //--------------------
                System.out.println(ticks.getSequential_id());
                ticksQueue.add(ticks);
                lastMaxSequentialId = Math.max(lastMaxSequentialId, ticks.getSequential_id());
                System.out.println("if = "+ticksQueue.size());
            }
        }
        System.out.println(ticksQueue.size());
        tickService.processVWAP();
        virtualMarketService.getVirtualMarketPrice(ticksQueue);
    }
    @Override
    public void destroy() throws Exception { //담긴 Queue데이터 확인용
        log.info("종료 전 큐 데이터 출력");
        ticksQueue.forEach(tick -> log.info(tick.toString())); //
        log.info("총 {}건의 데이터 출력 완료",ticksQueue.size());
//        ticksQueue.clear();
    }


}
