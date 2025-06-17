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


    //전날 종가 데이터 구독 목록 관리
    private final Set<String> PrevRateSubscribedTickers = ConcurrentHashMap.newKeySet();


    /*
    실시간 체결 내역 구독
     */
    public void subscribeRealTimeTradeRate(String ticker) {
        validateTicker(ticker);
        log.debug("실시간 체결 정보 티커 구독 추가: {}", ticker);
        realTimeTradeRateSubscribedTickers.add(ticker);
    }

    //구독 해지
    public void unsubscribeRealTimeTradeRate(String ticker) {
        validateTicker(ticker);
        log.debug("실시간 체결 정보 티커 구독 해지: {}", ticker);
        realTimeTradeRateSubscribedTickers.remove(ticker);
    }

    //모든 구독 종목 반환
    public Set<String> getAllRealTimeTradeRateSubscribedTickers() {
        return realTimeTradeRateSubscribedTickers;
    }

    //종목에 대한 구독 여부
    public boolean isSubscribedToRealTimeTradeRate(String ticker) {
        validateTicker(ticker);
        return realTimeTradeRateSubscribedTickers.contains(ticker);
    }


    /**
     * 실시간 OHLC 티커 구독 추가
     */
    public void subscribeRealTimeOhlc(String ticker) {
        validateTicker(ticker);
        log.debug("실시간 OHLC 티커 구독 추가: {}", ticker);
        realTimeOhlcSubscribedTickers.add(ticker);
    }

    public void unsubscribeRealTimeOhlc(String ticker) {
        validateTicker(ticker);
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
        validateTicker(ticker);
        return realTimeOhlcSubscribedTickers.contains(ticker);
    }


    /*
    전날 종가 변동률 구독 추가,삭제,조회
     */
    public void subscribePrevRate(String ticker) {
        validateTicker(ticker);
        log.debug("전날 종가 변동률 티커 구독 추가: {}", ticker);
        PrevRateSubscribedTickers.add(ticker);
    }

    public void unsubscribePrevRate(String ticker) {
        validateTicker(ticker);
        log.debug("전날 종가 변동률 티커 구독 해지: {}", ticker);
        PrevRateSubscribedTickers.remove(ticker);
    }

    public Set<String> getAllPrevRateSubscribedTickers(String ticker) {
        return PrevRateSubscribedTickers;
    }

    public boolean isSubscribedToPrevRate(String ticker) {
        validateTicker(ticker);
        return PrevRateSubscribedTickers.contains(ticker);
    }

    //CCmap은 null을 허용시키기때문에 null 종목이 들어가도 npe발생안되는 이슈 테스트에서 발견
    //검증 로직 추가
    private void validateTicker(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 티커입니다: " + ticker);
        }
    }

}