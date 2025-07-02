package com.cleanengine.coin.orderbook.application.service;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.domain.spi.ActiveOrders;
import com.cleanengine.coin.order.domain.spi.ActiveOrdersManager;
import com.cleanengine.coin.orderbook.domain.OrderBookDomainService;
import com.cleanengine.coin.orderbook.dto.ClosingPriceDto;
import com.cleanengine.coin.orderbook.dto.OrderBookInfo;
import com.cleanengine.coin.orderbook.dto.OrderBookUnitInfo;
import com.cleanengine.coin.orderbook.infra.TradeQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.cleanengine.coin.common.CommonValues.approxEquals;

@Component
@RequiredArgsConstructor
public class OrderBookService implements UpdateOrderBookUsecase, ReadOrderBookUsecase {
    private final ActiveOrdersManager activeOrdersManager;
    private final OrderBookDomainService orderBookDomainService;
    private final OrderBookUpdatedNotifierPort orderBookUpdatedNotifierPort;
    private final TradeQueryService tradeQueryService;

    @Override
    public void updateOrderBookOnNewOrder(Order order) {
        addToOrderBook(order, order.getOrderSize());
    }

    @Override
    public void updateOrderBookOnRestored(Order order) {
        addToOrderBook(order, order.getRemainingSize());
    }

    private void addToOrderBook(Order order, double size) {
        if(order.getIsMarketOrder()){return;}
        String ticker = order.getTicker();
        activeOrders(ticker).saveOrder(order);

        boolean isBuyOrder = order instanceof BuyOrder;
        orderBookDomainService.updateOrderBookOnNewOrder(ticker, isBuyOrder, order.getPrice(), size);

        sendOrderBookUpdated(ticker);
    }

    @Override
    public void updateOrderBookOnTradeExecuted(String ticker, Long buyOrderId, Long sellOrderId, Double orderSize) {
        updateOrderBookOnTradeExecuted(ticker, buyOrderId, true, orderSize);
        updateOrderBookOnTradeExecuted(ticker, sellOrderId, false, orderSize);

        sendOrderBookUpdated(ticker);
    }

    private void updateOrderBookOnTradeExecuted(String ticker, Long orderId, boolean isBuyOrder, Double orderSize) {
        ActiveOrders activeOrders = activeOrders(ticker);

        Optional<Order> orderOptional = activeOrders.getOrder(orderId, isBuyOrder);
        // 시장가일 경우에는 ManagerPool에 없음
        if(orderOptional.isPresent()){
            Order order = orderOptional.get();
            orderBookDomainService.updateOrderBookOnTradeExecuted(ticker, isBuyOrder, order.getPrice(), orderSize);
            if(order.getState().equals(OrderStatus.DONE) || approxEquals(order.getOrderSize(), 0.0)){
                activeOrders.removeOrder(orderId, isBuyOrder);
            }
        }
    }

    private OrderBookInfo extractOrderBookInfo(String ticker){
        ClosingPriceDto finalClosingPriceDto = getYesterdayClosingPrice(ticker);

        List<OrderBookUnitInfo> buyOrderBookUnitInfos =
                orderBookDomainService.getBuyOrderBookList(ticker, 10)
                        .stream()
                        .map(orderBookUnit -> new OrderBookUnitInfo(orderBookUnit, finalClosingPriceDto.closingPrice()))
                        .toList();
        List<OrderBookUnitInfo> sellOrderBookUnitInfos =
                orderBookDomainService.getSellOrderBookList(ticker, 10)
                        .stream()
                        .map(orderBookUnit -> new OrderBookUnitInfo(orderBookUnit, finalClosingPriceDto.closingPrice()))
                        .toList();

        return new OrderBookInfo(ticker, buyOrderBookUnitInfos, sellOrderBookUnitInfos);
    }

    private ClosingPriceDto getYesterdayClosingPrice(String ticker){
        LocalDate yesterday = LocalDate.now().minusDays(1);
        ClosingPriceDto closingPriceDto = tradeQueryService.getYesterdayClosingPrice(ticker, yesterday);

        if(closingPriceDto == null) {
            closingPriceDto = new ClosingPriceDto(ticker, yesterday, 0.0);
        }

        return closingPriceDto;
    }

    private void sendOrderBookUpdated(String ticker){
        orderBookUpdatedNotifierPort.sendOrderBooks(extractOrderBookInfo(ticker));
    }

    @Override
    public OrderBookInfo getOrderBook(String ticker) {
        return extractOrderBookInfo(ticker);
    }

    private ActiveOrders activeOrders(String ticker){
        return activeOrdersManager.getActiveOrders(ticker);
    }
}
