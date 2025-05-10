package com.cleanengine.coin.order.application;

import com.cleanengine.coin.order.application.strategy.CreateOrderStrategy;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    @Transactional
    public OrderInfo<?> createOrderWithBot(OrderCommand.CreateOrder createOrder){
        LocalDateTime createdAt = LocalDateTime.now();
        OrderCommand.CreateOrder orderCommandWithBot =
                new OrderCommand.CreateOrder(createOrder.ticker(), createOrder.userId(), createOrder.isBuyOrder(),
                        createOrder.isMarketOrder(), createOrder.orderSize(), createOrder.price(), createdAt, true);

        return createOrder(orderCommandWithBot);
    }
}

