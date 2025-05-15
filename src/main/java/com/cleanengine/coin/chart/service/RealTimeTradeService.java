package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.RealTimeDataDto;
import com.cleanengine.coin.chart.dto.TradeEventDto;
import com.cleanengine.coin.trade.application.TradeBatchProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeTradeService {
    //tradeService를 받아야한다
    private final TradeBatchProcessor tradeBatchProcessor;

    //실시간 거래 데이터 보안성에 적합한 ConcuerrentHashMap 사용
    //전에 데이터를 캐싱처리해서 변동률 계산
    private final Map<String, TradeEventDto> previousTradeMap = new ConcurrentHashMap<>();

    public RealTimeDataDto generateRealTimeData(String ticker){
        // 최신 거래 이벤트 데이터 조회
        TradeEventDto tradeEventDto = tradeBatchProcessor.retrieveTradeEventDto(ticker);

        // 거래 데이터가 없는 경우 처리
        if(tradeEventDto == null){
            log.warn("실시간 거래 데이터가 존재하지않습니다: {}", ticker);
            return new RealTimeDataDto(ticker, 0, 0, 0, LocalDateTime.now(), UUID.randomUUID().toString());
        }

        // 현재 가격 및 시간 정보 추출
        double currentPrice = tradeEventDto.getPrice();
        double currentSize = tradeEventDto.getSize();
        LocalDateTime currentTime = tradeEventDto.getTimestamp();

        // 변동률 계산
        double changeRate = 0.0;
        TradeEventDto previousTrade = previousTradeMap.get(ticker);

        if (previousTrade != null && previousTrade.getPrice() > 0) {
            // 이전 거래 정보가 있는 경우 변동률 계산
            double previousPrice = previousTrade.getPrice();
            changeRate = ((currentPrice - previousPrice) / previousPrice) * 100;
            log.debug("변동률 계산: 현재가={}, 이전가={}, 변동률={}%",
                    currentPrice, previousPrice, changeRate);
        } else {
            log.debug("이전 거래 정보가 없어 변동률을 0으로 설정: {}", ticker);
        }

        // 현재 거래 정보를 이전 거래 정보로 캐시에 저장
        previousTradeMap.put(ticker, tradeEventDto);

        // RealTimeDataDto 객체 생성 및 반환
        return new RealTimeDataDto(
                ticker,
                currentSize,
                currentPrice,
                changeRate,
                currentTime,
                UUID.randomUUID().toString()
        );
    }
}