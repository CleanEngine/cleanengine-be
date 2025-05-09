package com.cleanengine.coin.order.application;

import com.cleanengine.coin.order.application.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.domainservice.CreateOrderDomainService;
import com.cleanengine.coin.order.infra.BuyOrderRepository;
import com.cleanengine.coin.order.infra.SellOrderRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class OrderService {
    private final BuyOrderRepository buyOrderRepository;
    private final SellOrderRepository sellOrderRepository;
    private final CreateOrderDomainService createOrderDomainService;
    private final OrderQueueManagerPool orderQueueManagerPool;

    @Transactional
    public OrderInfo createOrder(@Valid OrderCommand.CreateOrder createOrder){
        Order order = createOrderDomainService.createOrder(createOrder.ticker(), createOrder.userId(),
                createOrder.isBuyOrder(), createOrder.isMarketOrder(), createOrder.orderSize(),
                createOrder.price(),createOrder.createdAt(),createOrder.isBot());

        if(order instanceof BuyOrder){
            buyOrderRepository.save((BuyOrder) order);
        }else{
            sellOrderRepository.save((SellOrder) order);
        }

        orderQueueManagerPool.addOrder(order.getTicker(), order);

        return OrderInfo.builder()
                .id(order.getId())
                .createdAt(order.getCreatedAt()).build();
    }
}

