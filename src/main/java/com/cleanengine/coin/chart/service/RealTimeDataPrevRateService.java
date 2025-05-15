package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.PrevRateDto;
import com.cleanengine.coin.chart.repository.RealTimeTradeRepository;
import com.cleanengine.coin.trade.entity.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeDataPrevRateService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RealTimeTradeRepository tradeRepository;

    // 기존 메서드 - 컨트롤러에서 호출된 후 내부적으로 전송
    public void publishPrevRate(String ticker) {
        PrevRateDto data = generatePrevRateData(ticker);
        messagingTemplate.convertAndSend("/topic/prevRate/" + ticker, data);
        log.debug("전송 완료: /topic/prevRate/{} -> {}", ticker, data);
    }

    // 새로운 메서드 - 컨트롤러에서 데이터만 가져오기 위해 사용
    public PrevRateDto generatePrevRateData(String ticker) {
        // 전일 종가 계산
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterdayStart = today.minusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yesterdayEnd = today.minusDays(1).withHour(23).withMinute(59).withSecond(59);

        Trade yesterdayLastTrade = tradeRepository.findFirstByTickerAndTradeTimeBetweenOrderByTradeTimeDesc(
                ticker, yesterdayStart, yesterdayEnd);

        // 현재가
        Trade currentTrade = tradeRepository.findFirstByTickerOrderByTradeTimeDesc(ticker);

        if (yesterdayLastTrade == null || currentTrade == null) {
            log.warn("전일 또는 현재 거래 데이터가 없습니다: {}", ticker);
            return new PrevRateDto(ticker, 0.0, 0.0, 0.0, LocalDateTime.now());
        }

        double prevClose = yesterdayLastTrade.getPrice();
        double currentPrice = currentTrade.getPrice();
        double changeRate = ((currentPrice - prevClose) / prevClose) * 100;

        return new PrevRateDto(
                ticker,
                prevClose,
                currentPrice,
                changeRate,
                LocalDateTime.now()
        );
    }
}