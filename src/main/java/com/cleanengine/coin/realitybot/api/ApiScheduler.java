package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.common.annotation.WorkingServerProfile;
import com.cleanengine.coin.order.adapter.out.persistentce.asset.AssetRepository;
import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.realitybot.domain.APIVWAPState;
import com.cleanengine.coin.realitybot.domain.VWAPMetricsRecorder;
import com.cleanengine.coin.realitybot.dto.Ticks;
import com.cleanengine.coin.realitybot.parser.CoinoneTicksAdapter;
import com.cleanengine.coin.realitybot.parser.TickParser;
import com.cleanengine.coin.realitybot.service.OrderGenerateService;
import com.cleanengine.coin.realitybot.service.TickServiceManager;
import com.google.common.util.concurrent.RateLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
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
    private final CoinoneAPIClient coinoneAPIClient;
    private final CoinoneTicksAdapter coinoneTicksAdapter;
    private final VWAPMetricsRecorder recorder;
    private final MeterRegistry meterRegistry;
//    private final BotThreadConfig executor;
    private final RateLimiter rateLimiter = RateLimiter.create(140.0); //TODO 마켓별로 커스텀 필요
    private String ticker;

//    @Scheduled(fixedRate = 5000)
    public void getMarketAllRequest() throws InterruptedException {
        Timer timer = meterRegistry.timer("apischeduler.request.duration");
        timer.record(() -> {
            List<Asset> tickers = assetRepository.findAll();
            for (Asset ticker : tickers) {
                String tickerName = ticker.getTicker();
//                executor.botThreadPoolExecutor().execute(() -> {
                    rateLimiter.acquire();
                     log.debug("[{}] 요청 실행: {}", LocalTime.now(), tickerName);
                getMarketDataRequest(tickerName); //failover 전략 추가
//                });
            }
        });
    }

    public void getMarketDataRequest(String ticker){
        this.ticker = ticker;
//        String rawJson = getMarketDataWithFallback(ticker);
        List<Ticks> gson = getMarketDataWithFallback(ticker); //json을 list로 변환

        //---------------- 이거 변환소로 바꾸기

        APIVWAPState apiVWAPState = tickServiceManager.getService(ticker);
        long lastSeqId = lastSequentialIdMap.getOrDefault(ticker,0L);

        //api 중복검사하여 queue에 저장하기
        for (int i = gson.size()-1; i >=0 ; i--) {//2차 : 10 - 역순으로 정렬되어 - 순회해야 함.
            Ticks ticks = gson.get(i);
            if (ticks.getSequential_id() > lastSeqId){ //중복 검증용
                apiVWAPState.addTick(ticks);
                lastSeqId = Math.max(lastSeqId, ticks.getSequential_id()); //중복 id 갱신

            }
        }
        lastSequentialIdMap.put(ticker,lastSeqId);
        double vwap = apiVWAPState.getVWAP();
        double volume = apiVWAPState.getAvgVolumePerOrder();
        recorder.recordApiVwap(ticker,vwap);

        orderGenerateService.generateOrder(ticker,vwap,volume); //1tick 당 매수/매도 3개씩 제작

        log.info("작동확인 {}의 가격 : {} , 볼륨 : {}",ticker, vwap, volume);
    }

public List<Ticks> getMarketDataWithFallback(String ticker) {
    try {
        String bithumbJson = bithumbAPIClient.get(ticker);
        // 예외가 없었어도 비정상 응답일 수 있음 → 예: 빈 JSON 또는 에러 코드
        if (bithumbJson != null && !bithumbJson.isBlank() && !bithumbJson.contains("\"result\":\"error\"")) {
            return tickParser.parseGson(bithumbJson);
        }
        log.warn("Bithumb 응답 비정상, Coinone으로 Failover: ticker{}",ticker);
    }catch (Exception e){
        log.error("Bithumb API 오류: ticker={}, 오류={}. Coinone으로 Failover", ticker, e.getMessage());
    }
    try {
        String coinoneJson = coinoneAPIClient.get(ticker);
        // 예외가 없었어도 비정상 응답일 수 있음 → 예: 빈 JSON 또는 에러 코드
        if (coinoneJson != null && !coinoneJson.isBlank() && !coinoneJson.contains("\"result\":\"error\"")) {
            return coinoneTicksAdapter.convertToTicks(coinoneJson);
        }
        log.warn("Coinone 응답 비정상: ticker{}",ticker);
    }catch (Exception e){
        log.error("Coinone API 오류: ticker={}, 오류={}. Coinone으로 Failover", ticker, e.getMessage());
    }
    return List.of();

}

}
