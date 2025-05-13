package com.cleanengine.coin.orderbook.application.service;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.infra.ActiveOrderManagerPool;
import com.cleanengine.coin.orderbook.domain.OrderBookDomainService;
import com.cleanengine.coin.orderbook.dto.OrderBookInfo;
import com.cleanengine.coin.orderbook.dto.OrderBookUnitInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.cleanengine.coin.common.CommonValues.approxEquals;

@Component
@RequiredArgsConstructor
public class OrderBookService implements UpdateOrderBookUsecase, ReadOrderBookUsecase {
    private final ActiveOrderManagerPool activeOrderManagerPool;
    private final OrderBookDomainService orderBookDomainService;
    private final OrderBookUpdatedNotifierPort orderBookUpdatedNotifierPort;

    @Override
    public void updateOrderBookOnNewOrder(Order order) {
        if(order.getIsMarketOrder()){return;}
        String ticker = order.getTicker();
        activeOrderManagerPool.saveOrder(ticker, order);

        boolean isBuyOrder = order instanceof BuyOrder;
        orderBookDomainService.updateOrderBookOnNewOrder(ticker, isBuyOrder, order.getPrice(), order.getOrderSize());

        sendOrderBookUpdated(ticker);
    }

    @Override
    public void updateOrderBookOnTradeExecuted(String ticker, Long buyOrderId, Long sellOrderId, Double orderSize) {
        updateOrderBookOnTradeExecuted(ticker, buyOrderId, true, orderSize);
        updateOrderBookOnTradeExecuted(ticker, sellOrderId, false, orderSize);

        sendOrderBookUpdated(ticker);
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

    private OrderBookInfo extractOrderBookInfo(String ticker){
        List<OrderBookUnitInfo> buyOrderBookUnitInfos =
                orderBookDomainService.getBuyOrderBookList(ticker, 10)
                        .stream().map(OrderBookUnitInfo::new).toList();
        List<OrderBookUnitInfo> sellOrderBookUnitInfos =
                orderBookDomainService.getSellOrderBookList(ticker, 10)
                        .stream().map(OrderBookUnitInfo::new).toList();
        return new OrderBookInfo(ticker, buyOrderBookUnitInfos, sellOrderBookUnitInfos);
    }

    private void sendOrderBookUpdated(String ticker){
        orderBookUpdatedNotifierPort.sendOrderBooks(extractOrderBookInfo(ticker));
    }

    @Override
    public OrderBookInfo getOrderBook(String ticker) {
        return extractOrderBookInfo(ticker);
    }
}
