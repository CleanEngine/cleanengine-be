package com.cleanengine.coin.chart.handler;

import com.cleanengine.coin.chart.dto.RealTimeDataDto;
import com.cleanengine.coin.chart.dto.TradeEventDto;
import com.cleanengine.coin.chart.service.ChartSubscriptionService; // 의존성 추가
import com.cleanengine.coin.chart.service.RealTimeTradeService;
import com.cleanengine.coin.chart.service.WebsocketSendService;
import com.cleanengine.coin.trade.application.TradeExecutedEvent;
import com.cleanengine.coin.trade.entity.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeEventHandler {
    private final RealTimeTradeService realTimeTradeService;
    private final WebsocketSendService websocketSendService;
    private final ChartSubscriptionService chartSubscriptionService; // 주입

    //event로 이벤틀 처리해야한다.
    //eventListener는 void로 처리를 해야한다
    @EventListener
    public void handleTradeEvent(TradeExecutedEvent event) {
        Trade trade = event.getTrade();
        if (trade == null) {
            log.warn("Trade 객체가 null");
            return;
        }

        String ticker = trade.getTicker();
        log.debug("TradeEventHandler 수신 : {}", trade);

        // 해당 종목에 대한 구독자가 있는지 확인
        if (chartSubscriptionService.isSubscribedToRealTimeTradeRate(ticker)) {
            log.debug("종목 {} 실시간 체결 정보 구독자 확인됨. 데이터 처리 및 전송 시작.", ticker);
            TradeEventDto tradeEventDto = getTradeEventDto(trade);

            //실시간 체결가 및 변동률 전송
            try {
                RealTimeDataDto dto = realTimeTradeService.generateRealTimeData(tradeEventDto);
                websocketSendService.send(dto, dto.getTicker()); // dto.getTicker()는 이미 ticker와 동일
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