
package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.RealTimeDataDto;
import com.cleanengine.coin.chart.dto.TradeEventDto;
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
    //실시간 거래 데이터 보안성에 적합한 ConcurrentHashMap 사용
    //전에 데이터를 캐싱처리해서 변동률 계산
    private final Map<String, TradeEventDto> previousTradeMap = new ConcurrentHashMap<>();

    //이벤트 Dto 받아서 체결내역에 필요한 데이터들을 보내주는것
    public RealTimeDataDto generateRealTimeData(TradeEventDto tradeEventDto) {
        try {
            TradeInfo currentTradeInfo = extractTradeInfo(tradeEventDto);

            ChangeRateResult changeRateResult = calculateChangeRate(tradeEventDto, currentTradeInfo);

            updateTradeCache(tradeEventDto, changeRateResult);

            return createRealTimeDataDto(currentTradeInfo, changeRateResult.changeRate());

        } catch (Exception e) {
            log.error("실시간 데이터 생성 중 오류: {}", e.getMessage(), e);
            // 기본값으로 DTO 생성
            TradeInfo currentTradeInfo = extractTradeInfo(tradeEventDto);
            return createRealTimeDataDto(currentTradeInfo, 0.0);
        }
    }

    // 거래 정보 추출
    TradeInfo extractTradeInfo(TradeEventDto tradeEventDto) {
        return new TradeInfo(
                tradeEventDto.getTicker(),
                tradeEventDto.getPrice(),
                tradeEventDto.getSize(),
                tradeEventDto.getTimestamp()
        );
    }

    // 변동률 계산
    ChangeRateResult calculateChangeRate(TradeEventDto currentTrade, TradeInfo currentTradeInfo) {
        TradeEventDto previousTrade = previousTradeMap.get(currentTradeInfo.ticker());

        logTradeComparison(currentTrade, previousTrade);

        if (!shouldCalculateChangeRate(previousTrade, currentTrade)) {
            return new ChangeRateResult(0.0, false);
        }

        if (!isNewTrade(previousTrade, currentTrade)) {
            log.debug("동일한 타임스탬프의 거래 데이터가 다시 수신됨: {}", currentTrade.getTimestamp());
            return new ChangeRateResult(0.0, false);
        }

        double changeRate = getChangeRate(currentTradeInfo.price(), previousTrade.getPrice());
        log.debug("변동률 계산: 현재가={}, 이전가={}, 변동률={}%",
                currentTradeInfo.price(), previousTrade.getPrice(), changeRate);

        return new ChangeRateResult(changeRate, true);
    }

    // 변동률 계산 조건 검사
    boolean shouldCalculateChangeRate(TradeEventDto previousTrade, TradeEventDto currentTrade) {
        if (previousTrade == null) {
            log.debug("이전 거래 정보가 없어 변동률을 0으로 설정: {}", currentTrade.getTicker());
            return false;
        }

        if (previousTrade.getPrice() <= 0) {
            log.debug("이전 거래 가격이 유효하지 않음: {}", previousTrade.getPrice());
            return false;
        }

        if (previousTrade == currentTrade) {
            log.debug("동일한 거래 객체가 다시 수신됨 (참조 동일): {}", currentTrade.getTicker());
            return false;
        }

        return true;
    }

    // 새로운 거래인지 판단
    boolean isNewTrade(TradeEventDto previousTrade, TradeEventDto currentTrade) {
        if (previousTrade.getTimestamp() == null || currentTrade.getTimestamp() == null) {
            return true;
        }
        return !previousTrade.getTimestamp().equals(currentTrade.getTimestamp());
    }

    // 거래 비교 로그
    void logTradeComparison(TradeEventDto currentTrade, TradeEventDto previousTrade) {
        log.debug("타임스탬프 비교 - 현재: {}, 이전: {}, 동일객체: {}",
                currentTrade.getTimestamp(),
                previousTrade != null ? previousTrade.getTimestamp() : "없음",
                previousTrade == currentTrade);
    }

    // 캐시 업데이트
    void updateTradeCache(TradeEventDto tradeEventDto, ChangeRateResult changeRateResult) {
        if (changeRateResult.shouldUpdate() || !previousTradeMap.containsKey(tradeEventDto.getTicker())) {
            TradeEventDto cachedTrade = createCachedTradeDto(tradeEventDto);
            previousTradeMap.put(tradeEventDto.getTicker(), cachedTrade);
        }
    }

    // 캐시용 TradeEventDto 생성 (복사본)
    TradeEventDto createCachedTradeDto(TradeEventDto cashDataDto) {
        return new TradeEventDto(
                cashDataDto.getTicker(),
                cashDataDto.getSize(),
                cashDataDto.getPrice(),
                cashDataDto.getTimestamp()
        );
    }

    // RealTimeDataDto 생성
    RealTimeDataDto createRealTimeDataDto(TradeInfo tradeInfo, double changeRate) {
        return new RealTimeDataDto(
                tradeInfo.ticker(),
                tradeInfo.size(),
                tradeInfo.price(),
                changeRate,
                tradeInfo.timestamp(),
                generateTransactionId()
        );
    }

    // 트랜잭션 ID 생성
    String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    // 변동률 계산
    public double getChangeRate(double currentPrice, double previousPrice) {
        return ((currentPrice - previousPrice) / previousPrice) * 100;
    }

    // 거래 정보를 담는 record
    record TradeInfo(String ticker, double price, double size, LocalDateTime timestamp) {}

    // 변동률 계산 결과를 담는 record
    record ChangeRateResult(double changeRate, boolean shouldUpdate) {}
}