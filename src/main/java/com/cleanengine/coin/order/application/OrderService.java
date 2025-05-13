package com.cleanengine.coin.order.application;

import com.cleanengine.coin.order.application.strategy.CreateOrderStrategy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class OrderService { //facade
    private final List<CreateOrderStrategy<?, ?>> createOrderStrategies;

    @Transactional
    public OrderInfo<?> createOrder(@Valid OrderCommand.CreateOrder createOrder){
        CreateOrderStrategy<?, ?> createOrderStrategy = createOrderStrategies.stream()
                .filter(strategy -> strategy.supports(createOrder.isBuyOrder())).findFirst().orElseThrow();
        return createOrderStrategy.processCreatingOrder(createOrder);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderInfo<?> createOrderWithBot(String ticker, Boolean isBuyOrder, Double orderSize, Double price){
        Integer userId = isBuyOrder? 1 : 2;

        OrderCommand.CreateOrder createOrder = new OrderCommand.CreateOrder(ticker, userId, isBuyOrder,
                false, orderSize, price, LocalDateTime.now(), true);

        return createOrder(createOrder);
    }
}
