package com.cleanengine.coin.chart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ChartSubscriptionService {
    // 구독된 티커 목록 관리
    private final Set<String> subscribedTickers = ConcurrentHashMap.newKeySet();

    // 실시간 OHLC 구독 목록 관리
    private final Set<String> realTimeOhlcSubscribedTickers = ConcurrentHashMap.newKeySet();

    // 실시간 거래 구독 목록 관리
    private final Set<String> realTimeTradeSubscribedTickers = ConcurrentHashMap.newKeySet();

    /**
     * 티커 구독 추가 (캔들)
     */
    public void subscribe(String ticker) {
        log.info("캔들 티커 구독 추가: {}", ticker);
        subscribedTickers.add(ticker);
    }

    /**
     * 티커 구독 해제 (캔들)
     */
    public void unsubscribe(String ticker) {
        log.info("캔들 티커 구독 해제: {}", ticker);
        subscribedTickers.remove(ticker);
    }

    /**
     * 모든 구독된 티커 조회 (캔들)
     */
    public Set<String> getAllSubscribedTickers() {
        return subscribedTickers;
    }

    /**
     * 해당 티커가 구독되었는지 확인 (캔들)
     */
    public boolean isSubscribed(String ticker) {
        return subscribedTickers.contains(ticker);
    }

    /**
     * 실시간 OHLC 티커 구독 추가
     */
    public void subscribeRealTimeOhlc(String ticker) {
        log.info("실시간 OHLC 티커 구독 추가: {}", ticker);
        realTimeOhlcSubscribedTickers.add(ticker);
    }

    /**
     * 실시간 OHLC 티커 구독 해제
     */
    public void unsubscribeRealTimeOhlc(String ticker) {
        log.info("실시간 OHLC 티커 구독 해제: {}", ticker);
        realTimeOhlcSubscribedTickers.remove(ticker);
    }

    /**
     * 모든 실시간 OHLC 구독된 티커 조회
     */
    public Set<String> getAllRealTimeOhlcSubscribedTickers() {
        return realTimeOhlcSubscribedTickers;
    }

    /**
     * 해당 티커가 실시간 OHLC 구독되었는지 확인
     */
    public boolean isRealTimeOhlcSubscribed(String ticker) {
        return realTimeOhlcSubscribedTickers.contains(ticker);
    }

    /**
     * 실시간 거래 데이터 티커 구독 추가
     */
    public void subscribeRealTime(String ticker) {
        log.info("실시간 거래 데이터 티커 구독 추가: {}", ticker);
        realTimeTradeSubscribedTickers.add(ticker);
    }

    /**
     * 실시간 거래 데이터 티커 구독 해제
     */
    public void unsubscribeRealTime(String ticker) {
        log.info("실시간 거래 데이터 티커 구독 해제: {}", ticker);
        realTimeTradeSubscribedTickers.remove(ticker);
    }

    /**
     * 모든 실시간 거래 데이터 구독된 티커 조회
     */
    public Set<String> getAllRealTimeSubscribedTickers() {
        return realTimeTradeSubscribedTickers;
    }

    /**
     * 해당 티커가 실시간 거래 데이터 구독되었는지 확인
     */
    public boolean isRealTimeSubscribed(String ticker) {
        return realTimeTradeSubscribedTickers.contains(ticker);
    }
}