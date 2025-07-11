package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.order.application.AssetService;
import com.cleanengine.coin.order.application.OrderCancelService;
import com.cleanengine.coin.order.application.dto.AssetInfo;
import com.cleanengine.coin.realitybot.dto.BotOrderCount;
import com.cleanengine.coin.realitybot.dto.BotOrderInfo;
import com.cleanengine.coin.realitybot.infra.BotOrderQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotOrderCancelService {
    private final OrderCancelService orderCancelService;
    private final BotOrderQueryRepository botOrderQueryRepository;
    private final AssetService assetService;
    private final ExecutorService executorService;

    public void cancelBotOrdersAllTicker(double cancelRate) {
        log.debug("cancelAllBotOrders started.");
        List<AssetInfo> assetInfos = assetService.getAllAssetInfos();

        log.debug("assetInfos.size() : {}", assetInfos.size());

        for(AssetInfo assetInfo : assetInfos){
            executorService.submit(() -> cancelBotLimitOrder(cancelRate, assetInfo.ticker()));
        }
    }

    public void cancelBotLimitOrder(double cancelRate, String ticker) {
        log.debug("cancelBotLimitOrder started.");

        BotOrderCount botOrderCountNeededToBeCanceled = getCountNeededToBeCanceled(cancelRate, ticker);
        log.debug("ticker : {}, buyOrderCount : {}, sellOrderCount : {}",
                ticker,
                botOrderCountNeededToBeCanceled.buyOrderCount(),
                botOrderCountNeededToBeCanceled.sellOrderCount());

        List<BotOrderInfo> orderIdsNeededToBeCanceled = getBotOrderInfosNeededToBeCanceled(
                botOrderCountNeededToBeCanceled, ticker);

        log.debug("orderIdsNeededToBeCanceled.size() : {}", orderIdsNeededToBeCanceled.size());

        for(BotOrderInfo botOrderInfo : orderIdsNeededToBeCanceled){
            cancelEachOrder(botOrderInfo);
        }
    }

    private BotOrderCount getCountNeededToBeCanceled(double cancelRate, String ticker) {
        log.debug("getCountNeededToBeCanceled started.");
        if(cancelRate <= 0 || cancelRate > 1) throw new IllegalArgumentException("cancelRate는 0과 1 사이");

        BotOrderCount botOrderCount = botOrderQueryRepository.countWaitingBotOrdersByTicker(ticker);

        return new BotOrderCount(
                (long)Math.floor(botOrderCount.buyOrderCount() * cancelRate),
                (long)Math.floor(botOrderCount.sellOrderCount() * cancelRate)
        );
    }

    private List<BotOrderInfo> getBotOrderInfosNeededToBeCanceled(BotOrderCount botOrderCount, String ticker) {
        return botOrderQueryRepository.findWaitingBotOrdersByTickerAndCount(ticker, botOrderCount);
    }

    private void cancelEachOrder(BotOrderInfo botOrderInfo) {
        try{
            log.debug("cancelBotOrder starts : {}", botOrderInfo);
            orderCancelService.cancelOrder(botOrderInfo.orderId(), botOrderInfo.userId());
        }
        catch (Exception e){
            log.warn("cancelBotOrder fails : {}", botOrderInfo);
            log.warn("cause : {}", e.getMessage());
            log.warn("stackTrace : {}", (Object[]) e.getStackTrace());
        }
    }
}
