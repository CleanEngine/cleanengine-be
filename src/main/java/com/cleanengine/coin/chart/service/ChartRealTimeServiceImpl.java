package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.Dto.RealTimeTradeDto;
import com.cleanengine.coin.chart.repository.RealTimeTradeRepository;
import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ChartRealTimeServiceImpl implements ChartRealTimeService{


    private final RealTimeTradeRepository realTimeTradeRepository;

    public ChartRealTimeServiceImpl(RealTimeTradeRepository realTimeTradeRepository) {
        this.realTimeTradeRepository = realTimeTradeRepository;
    }


    @Override
    public RealTimeTradeDto getRealTimeTrade(String ticker) {
        // 최신 거래 정보 조회
        Trade latestTrade = realTimeTradeRepository.findFirstByTickerOrderByTradeTimeDesc(ticker);

        if (latestTrade == null) {
            return null; // 거래 데이터가 없는 경우
        }

        // DTO로 변환
        RealTimeTradeDto dto = new RealTimeTradeDto();
        dto.setTicker(latestTrade.getTicker());
        dto.setPrice(latestTrade.getPrice());
        dto.setSize(latestTrade.getSize());

        // 변동률 계산
        double changeRate = calculateRoi(ticker);
        dto.setChangeRate(changeRate);

        return dto;
    }




    //변동률 계산하는것
    public double calculateRoi(String ticker) {
        // 현재 날짜/시간
        LocalDateTime now = LocalDateTime.now();

        // 어제 시작과 끝 시간 계산
        LocalDateTime yesterdayStart = now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime yesterdayEnd = now.withHour(0).withMinute(0).withSecond(0).withNano(0).minusNanos(1);

        // 전날의 마지막 체결 가져오기 (트레이드 시간 기준 내림차순 정렬 후 첫 번째 항목)
        Trade yesterdayLastTrade = realTimeTradeRepository.findFirstByTickerAndTradeTimeBetweenOrderByTradeTimeDesc(
                ticker, yesterdayStart, yesterdayEnd);

        if (yesterdayLastTrade == null) {
            // 전날 거래 데이터가 없는 경우
            return 0.0;  // 또는 다른 기본값이나 예외 처리
        }

        // 전날 마지막 체결가
        double yesterdayLastPrice = yesterdayLastTrade.getPrice();

        // 현재가 (오늘의 가장 최근 체결)
        Trade todayLatestTrade = realTimeTradeRepository.findFirstByTickerOrderByTradeTimeDesc(ticker);
        if (todayLatestTrade == null) {
            return 0.0;  // 오늘 거래가 없는 경우
        }

        double currentPrice = todayLatestTrade.getPrice();

        // 변동률 계산: ((현재가 - 전일 마지막가) / 전일 마지막가) * 100
        return ((currentPrice - yesterdayLastPrice) / yesterdayLastPrice) * 100;
    }

    // 변환 메서드는 구현체에서 private으로 정의
    private RealTimeTradeDto convertToRealTimeTradeDto(Trade trade) {
        if (trade == null) return null;

        RealTimeTradeDto dto = new RealTimeTradeDto();
        dto.setTicker(trade.getTicker());
        dto.setPrice(trade.getPrice());
        dto.setSize(trade.getSize());

        // 변동률 계산
        double changeRate = calculateRoi(trade.getTicker());
        dto.setChangeRate(changeRate);

        return dto;
    }



}
