package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.common.annotation.WorkingServerProfile;
import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.order.infra.AssetRepository;
import com.cleanengine.coin.realitybot.dto.Ticks;
import com.cleanengine.coin.realitybot.service.ApiVWAPService;
import com.cleanengine.coin.realitybot.service.OrderGenerateService;
import com.cleanengine.coin.realitybot.service.TickParser;
import com.cleanengine.coin.realitybot.service.TickServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@WorkingServerProfile
@RequiredArgsConstructor
public class ApiScheduler {

    private final BithumbAPIClient bithumbAPIClient;
    private final TickParser tickParser;
    private final OrderGenerateService orderGenerateService;
    private final TickServiceManager tickServiceManager;
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
                lastSeqId = Math.max(lastSeqId, ticks.getSequential_id()); //중복 id 갱신

            }
        }
        lastSequentialIdMap.put(ticker,lastSeqId);
        double vwap = apiVWAPService.getVWAP();
        double volume = apiVWAPService.getAvgVolumePerOrder();
        orderGenerateService.generateOrder(ticker,vwap,volume); //1tick 당 매수/매도 3개씩 제작
//        log.info("작동확인 {}의 가격 : {} , 볼륨 : {}",ticker, vwap, volume);
    }

/*    @Override
    public void destroy() throws Exception { //담긴 Queue데이터 확인용
//        log.info("종료 전 큐 데이터 출력");
//        ticksQueue.forEach(tick -> log.debug(tick.toString())); //
//        log.info("총 {}건의 데이터 출력 완료",ticksQueue.size());
//        orderQueueManagerService.logAllOrders();
//        virtualTradeService.printOrderSummary();
    }*/


}
