package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.order.infra.AssetRepository;
import com.cleanengine.coin.realitybot.dto.Ticks;
import com.cleanengine.coin.realitybot.service.ApiVWAPService;
import com.cleanengine.coin.realitybot.service.OrderGenerateService;
import com.cleanengine.coin.realitybot.service.TickParser;
import com.cleanengine.coin.realitybot.service.TickServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiScheduler implements DisposableBean {

    private final BithumbAPIClient bithumbAPIClient;
    private final TickParser tickParser;
    private final OrderGenerateService orderGenerateService;
    private final TickServiceManager tickServiceManager;
    private long lastMaxSequentialId = 1L;
    private final Map<String,Long> lastSequentialIdMap = new ConcurrentHashMap<>();
    private final AssetRepository assetRepository;
    private String ticker;

    @Scheduled(fixedRate = 5000)
    public void MarketAllRequest() throws InterruptedException {
        List<Asset> tickers = assetRepository.findAll();
        for (Asset ticker : tickers){
            String tickerName = ticker.getTicker();
            MarketDataRequest(tickerName);
            Thread.sleep(500);
        }
    }

    public void MarketDataRequest(String ticker){
        this.ticker = ticker;
        String rawJson = bithumbAPIClient.get(ticker); //api raw데이터
        List<Ticks> gson = TickParser.parseGson(rawJson); //json을 list로 변환

        ApiVWAPService apiVWAPService = tickServiceManager.getService(ticker);
        long lastSeqId = lastSequentialIdMap.getOrDefault(ticker,0L);

        //api 중복검사하여 queue에 저장하기
        for (int i = gson.size()-1; i >=0 ; i--) {//2차 : 10 - 역순으로 정렬되어 - 순회해야 함.
            Ticks ticks = gson.get(i);
            if (ticks.getSequential_id() > lastSeqId){ //중복 검증용
                apiVWAPService.addTick(ticks);
               /* //추세 갯수 지정 및 queue관리
                if (ticksQueue.size()>=10){ //갯수를 10개 까지만으로 제한
                    ticksQueue.poll();  //queue에서 빼기, 추세를 구현하기만 하면 필요가 있을까? -> 없음
                }//sequentialId가 역으로 받아 poll을 먼저해야 add가능

                //중복 검사 후 ticks 추가
                ticksQueue.add(ticks);*/
                lastSeqId = Math.max(lastSeqId, ticks.getSequential_id()); //중복 id 갱신

            }
        }
        lastSequentialIdMap.put(ticker,lastSeqId);

        //api 값으로 추세(VWAP)와 가상추세(VirtualVWAP) 구하기
        if (apiVWAPService.getTickSize()>=10){ //10개 이전 작동시 order 에런 발생 (-300~300원 대량주문 / 호가 단위 때문에)
            double vwap = apiVWAPService.getVWAP();
            double volume = apiVWAPService.getAvgVolumePerOrder();
//            tickService.processVWAP();//평균 체결 금액(VWAP) 구하기 (추세)

            //생성 된 vwap으로 주문 로직 실행 TODO 비동기로 전환하기
            orderGenerateService.generateOrder(ticker,vwap,volume); //1tick 당 매수/매도 3개씩 제작
            log.info("작동확인 {}의 가격 : {} , 볼륨 : {}",ticker, vwap, volume);
        } else {
            log.info("작동불가 {}는 갯수가 {}",ticker,apiVWAPService.getTickSize());
        };

    };
    @Override
    public void destroy() throws Exception { //담긴 Queue데이터 확인용
//        log.info("종료 전 큐 데이터 출력");
//        ticksQueue.forEach(tick -> log.info(tick.toString())); //
//        log.info("총 {}건의 데이터 출력 완료",ticksQueue.size());
//        orderQueueManagerService.logAllOrders();
//        virtualTradeService.printOrderSummary();
    }


}
