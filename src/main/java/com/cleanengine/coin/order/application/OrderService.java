package com.cleanengine.coin.order.application;

import com.cleanengine.coin.order.application.strategy.CreateOrderStrategy;
import com.cleanengine.coin.order.domain.Order;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;

@Service
@RequiredArgsConstructor
@Validated
public class OrderService { //facade
    private final List<CreateOrderStrategy<?, ?>> createOrderStrategies;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public OrderInfo<?> createOrder(@Valid OrderCommand.CreateOrder createOrder){
        CreateOrderStrategy<?, ?> createOrderStrategy = createOrderStrategies.stream()
                .filter(strategy -> strategy.supports(createOrder.isBuyOrder())).findFirst().orElseThrow();
        Order order  = createOrderStrategy.processCreatingOrder(createOrder);
        applicationEventPublisher.publishEvent(new OrderCreated(order));
        return createOrderStrategy.extractOrderInfo(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderInfo<?> createOrderWithBot(String ticker, Boolean isBuyOrder, Double orderSize, Double price){
        Integer userId = isBuyOrder? BUY_ORDER_BOT_ID : SELL_ORDER_BOT_ID;

        OrderCommand.CreateOrder createOrder = new OrderCommand.CreateOrder(ticker, userId, isBuyOrder,
                false, orderSize, price, LocalDateTime.now(), true);

        return createOrder(createOrder);
    }
}
