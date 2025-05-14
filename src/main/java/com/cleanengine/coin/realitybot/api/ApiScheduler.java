package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.realitybot.dto.Ticks;
import com.cleanengine.coin.realitybot.service.OrderGenerateService;
import com.cleanengine.coin.realitybot.service.TickService;
import com.cleanengine.coin.realitybot.service.VirtualTradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Queue;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiScheduler implements DisposableBean {

    private final BithumbAPIClient bithumbAPIClient;
    private final TickService tickService;
    private final VirtualMarketService virtualMarketService;
    private final OrderGenerateService orderGenerateService;
    private final OrderQueueManagerService orderQueueManagerService;
    private long lastMaxSequentialId = 1L;
    private final Queue<Ticks> ticksQueue;
    private final VirtualTradeService virtualTradeService;


//    @Scheduled(fixedRate = 5000) //5초마다 실행
    public void MarketDataRequest(){
        String rawJson = bithumbAPIClient.get(); //api raw데이터
        List<Ticks> gson = TickService.paraseGson(rawJson); //json을 list로 변환

        //api 중복검사하여 queue에 저장하기
        for (int i = gson.size()-1; i >=0 ; i--) {//2차 : 10 - 역순으로 정렬되어 - 순회해야 함.
            Ticks ticks = gson.get(i);
            if (ticks.getSequential_id() > lastMaxSequentialId){ //중복 검증용

                //추세 갯수 지정 및 queue관리
                if (ticksQueue.size()>=10){ //갯수를 10개 까지만으로 제한
                    ticksQueue.poll();  //queue에서 빼기, 추세를 구현하기만 하면 필요가 있을까? -> 없음
                }//sequentialId가 역으로 받아 poll을 먼저해야 add가능

                //중복 검사 후 ticks 추가
                ticksQueue.add(ticks);
                lastMaxSequentialId = Math.max(lastMaxSequentialId, ticks.getSequential_id()); //중복 id 갱신

                /*모니터링용
                System.out.println(ticks.getSequential_id());
                System.out.println("if = "+ticksQueue.size());
                */
            }
        }

        //api 값으로 추세(VWAP)와 가상추세(VirtualVWAP) 구하기
        if (ticksQueue.size()>=10){ //10개 이전 작동시 order 에런 발생 (-300~300원 대량주문 / 호가 단위 때문에)

            tickService.processVWAP();//평균 체결 금액(VWAP) 구하기 (추세)

            /*//모니터링용
            log.info("generateOrder vwap 확인용 = {}",tickService.getVwap());
            log.info("generateOrder volume 확인용 = {}",tickService.getTotalVolume());*/

            //생성 된 vwap으로 주문 로직 실행 TODO 비동기로 전환하기
            orderGenerateService.generateOrder(tickService.getVwap(),(tickService.getTotalVolume()/30)); //1tick 당 매수/매도 3개씩 제작
//            virtualTradeService.matchOrder();//일치하면 체결 진행 TODO 합칠 때 제거
            virtualTradeService.matchOrderbyIterator();//일치하면 체결 진행 TODO 합칠 때 제거
        };

    };
    @Override
    public void destroy() throws Exception { //담긴 Queue데이터 확인용
//        log.info("종료 전 큐 데이터 출력");
        ticksQueue.forEach(tick -> log.info(tick.toString())); //
//        log.info("총 {}건의 데이터 출력 완료",ticksQueue.size());
//        orderQueueManagerService.logAllOrders();
//        virtualTradeService.printOrderSummary();
    }


}
