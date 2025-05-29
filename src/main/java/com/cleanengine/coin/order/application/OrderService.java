package com.cleanengine.coin.order.application;

import com.cleanengine.coin.order.application.dto.OrderCommand;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.common.error.BusinessException;
import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.BuyOrderRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.SellOrderRepository;
import com.cleanengine.coin.order.application.strategy.CreateOrderStrategy;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class OrderService {
    private final List<CreateOrderStrategy<?, ?>> createOrderStrategies;
    private final BuyOrderRepository buyOrderRepository;
    private final SellOrderRepository sellOrderRepository;

    @Transactional
    public OrderInfo<?> createOrder(@Valid OrderCommand.CreateOrder createOrder){
        CreateOrderStrategy<?, ?> createOrderStrategy = createOrderStrategies.stream()
                .filter(strategy -> strategy.supports(createOrder.isBuyOrder())).findFirst().orElseThrow();

        return createOrderStrategy.processCreatingOrder(createOrder);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderInfo<?> createOrderWithBot(String ticker, Boolean isBuyOrder, Double orderSize, Double price){
        Integer userId = isBuyOrder? BUY_ORDER_BOT_ID : SELL_ORDER_BOT_ID;

        OrderCommand.CreateOrder createOrder = new OrderCommand.CreateOrder(ticker, userId, isBuyOrder,
                false, orderSize, price, LocalDateTime.now(), true);

        return createOrder(createOrder);
    }

    @Transactional
    public Order updateOrder(Order order){
        /*
        CreateOrderStrategy<?, ?> strategy = createOrderStrategies.stream()
                .filter(s -> s.supports(order instanceof BuyOrder))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Unsupported order type: " + order.getClass().getName(),
                        ErrorStatus.INTERNAL_SERVER_ERROR)
                );
        strategy.saveOrder(order);
        return order;
        */

        if (order instanceof BuyOrder) {
            return buyOrderRepository.save((BuyOrder) order);
        } else if (order instanceof SellOrder) {
            return sellOrderRepository.save((SellOrder) order);
        } else {
            throw new BusinessException("Unsupported order type: " + order.getClass().getName(), ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
