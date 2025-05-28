package com.cleanengine.coin.chart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ChartSubscriptionService {

    // 실시간 OHLC 구독 목록 관리
    //이건 종목에 따라 확장성을 고려해서 만든것 종목으로 불러오기
    private final Set<String> realTimeOhlcSubscribedTickers = ConcurrentHashMap.newKeySet();

    // 실시간 체결 정보 구독 목록 관리
    private final Set<String> realTimeTradeRateSubscribedTickers = ConcurrentHashMap.newKeySet();


    /*
    실시간 체결 내역 구독
     */
    public void subscribeRealTimeTradeRate(String ticker) {
        log.debug("실시간 체결 정보 티커 구독 추가: {}", ticker);
        realTimeTradeRateSubscribedTickers.add(ticker);
    }
    //구독 해지
    public void unsubscribeRealTimeTradeRate(String ticker) {
        log.debug("실시간 체결 정보 티커 구독 해지: {}", ticker);
        realTimeTradeRateSubscribedTickers.remove(ticker);
    }
    //모든 구독 종목 반환
    public Set<String> getAllRealTimeTradeRateSubscribedTickers() {
        return realTimeTradeRateSubscribedTickers;
    }
    //종목에 대한 구독 여부
    public boolean isSubscribedToRealTimeTradeRate(String ticker) {
        return realTimeTradeRateSubscribedTickers.contains(ticker);
    }


    /**
     * 실시간 OHLC 티커 구독 추가
     */
    public void subscribeRealTimeOhlc(String ticker) {
        log.debug("실시간 OHLC 티커 구독 추가: {}", ticker);
        realTimeOhlcSubscribedTickers.add(ticker);
    }

    public void unsubscribeRealTimeOhlc(String ticker) {
        log.debug("실시간 OHLC 티커 구독 해지: {}", ticker);
        realTimeOhlcSubscribedTickers.remove(ticker);
    }


    /**
     * 모든 실시간 OHLC 구독된 티커 조회
     */
    public Set<String> getAllRealTimeOhlcSubscribedTickers() {
        return realTimeOhlcSubscribedTickers;
    }

    public boolean isSubscribedToRealTimeOhlc(String ticker) {
        return realTimeOhlcSubscribedTickers.contains(ticker);
    }
}