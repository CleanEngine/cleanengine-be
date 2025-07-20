package com.cleanengine.coin.chart.handler;

import com.cleanengine.coin.chart.dto.PrevRateDto;
import com.cleanengine.coin.chart.dto.RealTimeDataDto;
import com.cleanengine.coin.chart.dto.TradeEventDto;
import com.cleanengine.coin.chart.service.ChartSubscriptionService; // 의존성 추가
import com.cleanengine.coin.chart.service.RealTimeDataPrevRateService;
import com.cleanengine.coin.chart.service.RealTimeTradeService;
import com.cleanengine.coin.chart.service.WebsocketSendService;
import com.cleanengine.coin.trade.domain.event.TradeExecutedEvent;
import com.cleanengine.coin.trade.domain.model.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeEventHandler {
    private final RealTimeTradeService realTimeTradeService;
    private final WebsocketSendService websocketSendService;
    private final ChartSubscriptionService chartSubscriptionService; // 주입
    private final RealTimeDataPrevRateService realTimeDataPrevRateService;

    //event로 이벤틀 처리해야한다.
    //eventListener는 void로 처리를 해야한다
    @TransactionalEventListener
    public void handleTradeEvent(TradeExecutedEvent event) {
        Trade trade = event.getTrade();
        if (trade == null) {
            log.warn("Trade 객체가 null");
            return;
        }

        String ticker = trade.getTicker();
        TradeEventDto tradeEventDto = getTradeEventDto(trade);
        log.debug("TradeEventHandler 수신 : {}", trade);

        // 실시간 데이터 전송
        processRealTimeTradeRate(ticker, tradeEventDto);

        //전날 종가 변동률 전송
        processPrevRateData(ticker, tradeEventDto);
    }

    public void processPrevRateData(String ticker, TradeEventDto tradeEventDto) {
        if (chartSubscriptionService.isSubscribedToPrevRate(ticker)) {
            log.debug("종목 {} 전일 대비 변동률 구독자 확인됨. 데이터 전송 시작.", ticker);
            try {
                PrevRateDto dto = realTimeDataPrevRateService.generatePrevRateData(tradeEventDto);
                websocketSendService.sendPrevRate(dto, dto.getTicker()); // dto.getTicker()는 이미 ticker와 동일
                log.debug("종목 {} 전일 대비 변동률 전송 완료 : {}", ticker, dto);
            } catch (Exception e) {
                log.error("종목 {} 전일 대비 변동률 전송 중 오류: {}", ticker, e.getMessage(), e);
            }
        } else {
            log.debug("종목 {} 전일 대비 변동률 구독자 없음. 데이터 전송 생략.", ticker);
        }
    }

    public void processRealTimeTradeRate(String ticker, TradeEventDto tradeEventDto) {
        if (chartSubscriptionService.isSubscribedToRealTimeTradeRate(ticker)) {
            log.debug("종목 {} 실시간 체결 정보 구독자 확인됨. 데이터 처리 및 전송 시작.", ticker);


            //실시간 체결가 및 변동률 전송
            try {
                RealTimeDataDto dto = realTimeTradeService.generateRealTimeData(tradeEventDto);
                websocketSendService.sendChangeRate(dto, dto.getTicker()); // dto.getTicker()는 이미 ticker와 동일
                log.debug("실시간 체결가 및 변동률 업데이트 전송 완료 : {}", ticker);
            } catch (Exception e) {
                log.error("종목 {} 실시간 체결가 및 변동률 업데이트 전송 중 오류: {}", ticker, e.getMessage(), e);
            }
        } else {
            log.debug("종목 {} 실시간 체결 정보 구독자 없음. 데이터 전송 생략.", ticker);
        }
    }

    @NotNull
    public static TradeEventDto getTradeEventDto(Trade trade) {
        return new TradeEventDto(
                trade.getTicker(),
                trade.getSize(),
                trade.getPrice(),
                trade.getTradeTime()
        );
    }

}