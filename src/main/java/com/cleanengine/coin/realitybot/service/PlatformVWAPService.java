package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.domain.PlatformVWAPState;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PlatformVWAPService {//TODO 가상 시장 조회용 사라질 예정임
    private final TradeRepository tradeRepository;
    private final MeterRegistry meterRegistry;
    Map<String, PlatformVWAPState> vwapMap = new ConcurrentHashMap<>();
    Map<String, LocalDateTime> lastTradeTimeMap = new ConcurrentHashMap<>();

    @WithSpan("api.request.02.order.platformvwap")
    public double calculateVWAPbyTrades(String ticker,double apiVWAP) {
        Timer timer = Timer.builder("db_query_execution_duration_seconds")
                .tag("query.type", "findTop10ByTicker")
                .tag("ticker", ticker)
                .register(meterRegistry);

        return timer.record(()->{
        PlatformVWAPState state = vwapMap.computeIfAbsent(ticker, PlatformVWAPState::new);
        LocalDateTime lastTradeTime = lastTradeTimeMap.get(ticker);



        //최근 체결 내역 가져오기
        List<Trade> trades = tradeRepository.findTop10ByTickerOrderByTradeTimeDesc(ticker);

        if ( trades.size() < 10){
            //체결 내역이 10개 이하일 경우 자체 계산
            return generateVWAP(apiVWAP);
        }
        LocalDateTime newestTime = trades.get(0).getTradeTime();
        if (lastTradeTime == null) {
            lastTradeTimeMap.put(ticker, newestTime);
            state.addTrades(trades);
            return state.getVWAP();
        }
        boolean containsSameTime = false;
        for (Trade trade : trades) {
            if (trade.getTradeTime().isEqual(lastTradeTime)) {
                containsSameTime = true;
                break;
            }
        }

        if (!containsSameTime) {
            trades = tradeRepository.findByTickerAndTradeTimeGreaterThanEqualOrderByTradeTimeDesc(ticker, lastTradeTime);
            newestTime = trades.get(0).getTradeTime();
            lastTradeTimeMap.put(ticker, newestTime);
        }

        //=================

        state.addTrades(trades);

        /*System.out.println("📦"+ticker+" [체결 기록]");
        state.addTrades(trades);trades.forEach(t ->
                System.out.printf("🕒 %s | 가격: %.0f | 수량: %.8f | 매수: #%d ↔ 매도: #%d%n",
                        t.getTradeTime(), t.getPrice(), t.getSize(), t.getBuyUserId(), t.getSellUserId())
        );*/
        return state.getVWAP();

        });
    }

        public double generateVWAP ( double apiVWAP){
            double maxDeviationaRate = 0.001; //보정값 0.1%만
            double deviation = (Math.random() * 2 - 1) * maxDeviationaRate; //편차 계산
            return apiVWAP * (1 + deviation); // +=deviation 난수 생성 후 계산 (범위는 -1~+1)
        }
    }