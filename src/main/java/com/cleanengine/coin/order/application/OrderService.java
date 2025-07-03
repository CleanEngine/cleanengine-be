package com.cleanengine.coin.order.application;

import com.cleanengine.coin.order.application.dto.OrderCommand;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.order.application.strategy.CreateOrderStrategy;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;

@Service
@RequiredArgsConstructor
@Validated
public class OrderService  {
    private final List<CreateOrderStrategy<?, ?>> createOrderStrategies;
    private final Validator validator;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderInfo<?> createOrder(OrderCommand.CreateOrder createOrder){
        validateCreateOrder(createOrder);
        CreateOrderStrategy<?, ?> createOrderStrategy = createOrderStrategies.stream()
                .filter(strategy -> strategy.supports(createOrder.isBuyOrder())).findFirst().orElseThrow();

        return createOrderStrategy.processCreatingOrder(createOrder);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void createOrderWithBot(String ticker, Boolean isBuyOrder, Double orderSize, Double price){
        Integer userId = isBuyOrder? BUY_ORDER_BOT_ID : SELL_ORDER_BOT_ID;

        OrderCommand.CreateOrder createOrder = new OrderCommand.CreateOrder(ticker, userId, isBuyOrder,
                false, orderSize, price, true);

        createOrder(createOrder);
    }

    protected void validateCreateOrder(OrderCommand.CreateOrder createOrder) {
        Set<ConstraintViolation<OrderCommand.CreateOrder>> violations = validator.validate(createOrder);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
