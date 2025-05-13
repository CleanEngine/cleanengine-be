package com.cleanengine.coin.orderbook.application.service;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.infra.ActiveOrderManagerPool;
import com.cleanengine.coin.orderbook.domain.OrderBookDomainService;
import com.cleanengine.coin.orderbook.domain.OrderPriceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.cleanengine.coin.common.CommonValues.approxEquals;

@Component
@RequiredArgsConstructor
public class OrderBookService implements UpdateOrderBookUsecase {
    private final ActiveOrderManagerPool activeOrderManagerPool;
    private final OrderBookDomainService orderBookDomainService;
    private final OrderBookUpdatedPort orderBookUpdatedPort;

    @Override
    public void updateOrderBookOnNewOrder(Order order) {
        if(order.getIsMarketOrder()){return;}
        activeOrderManagerPool.saveOrder(order.getTicker(), order);

        boolean isBuyOrder = order instanceof BuyOrder;
        orderBookDomainService.updateOrderBookOnNewOrder(order.getTicker(), isBuyOrder, order.getPrice(), order.getOrderSize());

        List<OrderPriceInfo> buyOrderBookList = orderBookDomainService.getBuyOrderBookList(order.getTicker(), 10);
        List<OrderPriceInfo> sellOrderBookList = orderBookDomainService.getSellOrderBookList(order.getTicker(), 10);
        orderBookUpdatedPort.sendOrderBooks(buyOrderBookList, sellOrderBookList);
    }

    @Override
    public void updateOrderBookOnTradeExecuted(String ticker, Long buyOrderId, Long sellOrderId, Double orderSize) {
        updateOrderBookOnTradeExecuted(ticker, buyOrderId, true, orderSize);
        updateOrderBookOnTradeExecuted(ticker, sellOrderId, false, orderSize);

        List<OrderPriceInfo> buyOrderBookList = orderBookDomainService.getBuyOrderBookList(ticker, 10);
        List<OrderPriceInfo> sellOrderBookList = orderBookDomainService.getSellOrderBookList(ticker, 10);
        orderBookUpdatedPort.sendOrderBooks(buyOrderBookList, sellOrderBookList);
    }

    private void updateOrderBookOnTradeExecuted(String ticker, Long orderId, boolean isBuyOrder, Double orderSize) {
        Optional<Order> orderOptional = activeOrderManagerPool.getOrder(ticker, orderId, isBuyOrder);
        // 시장가일 경우에는 ManagerPool에 없음
        if(orderOptional.isPresent()){
            Order order = orderOptional.get();
            orderBookDomainService.updateOrderBookOnTradeExecuted(ticker, isBuyOrder, order.getPrice(), orderSize);
            if(order.getState().equals(OrderStatus.DONE) || approxEquals(order.getOrderSize(), 0.0)){
                activeOrderManagerPool.removeOrder(ticker, orderId, isBuyOrder);
            }
        }
    }
}
