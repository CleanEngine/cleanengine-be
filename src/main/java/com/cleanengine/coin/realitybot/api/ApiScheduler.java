package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.realitybot.dto.Ticks;
import com.cleanengine.coin.realitybot.service.TickService;
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
//        for(Ticks ticks : gson){
        for (int i = gson.size()-1; i >=0 ; i--) {//10
            Ticks ticks = gson.get(i);
//            System.out.println("fori = " + gson.size());
            if (ticks.getSequential_id() > lastMaxSequentialId){
                System.out.println(ticks.getSequential_id());
                ticksQueue.add(ticks);
                lastMaxSequentialId = Math.max(lastMaxSequentialId, ticks.getSequential_id());
                System.out.println("if = "+ticksQueue.size());
            }
        }
        System.out.println(ticksQueue.size());
    }
    @Override
    public void destroy() throws Exception {
        log.info("종료 전 큐 데이터 출력");
        ticksQueue.forEach(tick -> log.info(tick.toString()));
        log.info("총 {}건의 데이터 출력 완료",ticksQueue.size());
//        ticksQueue.clear();
    }


}
