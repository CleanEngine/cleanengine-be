package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.common.annotation.StartNewTrace;
import com.cleanengine.coin.common.annotation.WorkingServerProfile;
import com.cleanengine.coin.order.adapter.out.persistentce.asset.AssetRepository;
import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.realitybot.config.BotThreadConfig;
import com.cleanengine.coin.realitybot.domain.APIVWAPState;
import com.cleanengine.coin.realitybot.domain.VWAPMetricsRecorder;
import com.cleanengine.coin.realitybot.dto.Ticks;
import com.cleanengine.coin.realitybot.parser.ExchangesParser;
import com.cleanengine.coin.realitybot.service.OrderGenerateService;
import com.cleanengine.coin.realitybot.service.TickServiceManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.util.concurrent.RateLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
@WorkingServerProfile
@RequiredArgsConstructor
public class ApiScheduler {
    private final List<ExchangesAPIClient> exchangesAPIClientList; // 5개 거래소 클라이언트
    private final Map<String, ExchangesParser> exchangesParserList; // 거래소별 파서
    private final OrderGenerateService orderGenerateService;
    private final TickServiceManager tickServiceManager;
    private final Map<String,Long> lastSequentialIdMap = new ConcurrentHashMap<>();
    private final AssetRepository assetRepository;
    private final VWAPMetricsRecorder recorder;
    private final MeterRegistry meterRegistry;
    @Qualifier("tickerExecutor")
    private final Executor tickerExecutor;
    @Qualifier("exchangeExecutor")
    private final Executor exchangeExecutor;
    private final RateLimiter rateLimiter = RateLimiter.create(140.0); //TODO 마켓별로 커스텀 필요!
    private String ticker;

//    @Scheduled(fixedRate = 5000)
    @StartNewTrace("api.request")
    public void getMarketAllRequest() throws InterruptedException {
        Timer timer = meterRegistry.timer("market.all.request.duration");
        timer.record(() -> {
            List<Asset> tickers = assetRepository.findAll();
            List<CompletableFuture<Void>> futures = tickers.stream()
                    .map(ticker -> CompletableFuture.runAsync(()->processTicker(ticker.getTicker()),tickerExecutor)).toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

//            for (Asset ticker : tickers) {
//                String tickerName = ticker.getTicker();
//                processTicker(tickerName);
//                executor.botThreadPoolExecutor().execute(() -> {
//                    rateLimiter.acquire(); //TODO 초반 테스트시 주석 후 실행 (멀티 스레드와 차이 비교 필요... 초당 140회 절대 불가능함..)
//                     log.debug("[{}] 요청 실행: {}", LocalTime.now(), tickerName);
//                });
//            }
        });
    }
    @WithSpan("api.request.01.processTicker")
    public void processTicker(String ticker){

//        long totalStart = System.currentTimeMillis();
        List<CompletableFuture<List<Ticks>>> exchangeFutures = exchangesAPIClientList.stream()
                .<CompletableFuture<List<Ticks>>>map(exchanges -> CompletableFuture.supplyAsync(()->{
                    try{
                        String json = exchanges.get(ticker);
                        ExchangesParser parser = exchangesParserList.get(exchanges.getExchangeName());
//                        log.info("processTicker 응답 확인 {} 거래소의 응답 : {}",exchanges.getExchangeName(),json);
                        return parser.parseJson(json,ticker);
                    } catch (JsonProcessingException e) {
//                        throw new RuntimeException(e);
                        return Collections.emptyList();
                    }
                }, exchangeExecutor)).toList();

        CompletableFuture.allOf(exchangeFutures.toArray(new CompletableFuture[0])).thenRun(()->{
            List<List<Ticks>> result = exchangeFutures.stream().map(CompletableFuture::join).toList();
            APIVWAPState state = tickServiceManager.getService(ticker);
            result.forEach(ticks -> ticks.forEach(state::addTick)); // ticks에 한번에 계산 , 계속 주입
            double vwap = state.getVWAP();
            double volume = state.getAvgVolumePerOrder();
            recorder.recordApiVwap(ticker,vwap);
            orderGenerateService.generateOrder(ticker,vwap,volume); //1tick 당 매수/매도 3개씩 제작
        }).join();

//        long totalEnd = System.currentTimeMillis();
//        log.info("✅ {}의 처리시간: {} ms",ticker,totalEnd-totalStart);
   }

}
