package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.order.application.OrderCommand;
import com.cleanengine.coin.order.application.OrderInfo;
import com.cleanengine.coin.order.application.port.AccountUpdatePort;
import com.cleanengine.coin.order.application.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.domainservice.CreateBuyOrderDomainService;
import com.cleanengine.coin.order.infra.BuyOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BuyOrderStrategy extends CreateOrderStrategy<BuyOrder, OrderInfo<BuyOrder>> {
    private final BuyOrderRepository buyOrderRepository;
    private final CreateBuyOrderDomainService createOrderDomainService;
    private final OrderQueueManagerPool orderQueueManagerPool;
    private final AccountUpdatePort accountUpdatePort;

    // TODO buyOrder만의 검증 내용 포함
    @Override
    public BuyOrder createOrder(OrderCommand.CreateOrder createOrderCommand) {
        return createOrderDomainService.createOrder(createOrderCommand.ticker(), createOrderCommand.userId(),
                createOrderCommand.isBuyOrder(), createOrderCommand.isMarketOrder(), createOrderCommand.orderSize(),
                createOrderCommand.price(), createOrderCommand.createdAt(), createOrderCommand.isBot());
    }

    @Override
    public void saveOrder(BuyOrder order) {
        buyOrderRepository.save(order);
    }

    @Override
    public OrderInfo.BuyOrderInfo extractOrderInfo(BuyOrder order) {
        return new OrderInfo.BuyOrderInfo(order);
    }

    @Override
    public boolean supports(Boolean isBuyOrder) {
        return isBuyOrder;
    }

    @Override
    protected void keepHoldings(BuyOrder order) throws RuntimeException {
        accountUpdatePort.lockDepositForBuyOrder(order.getUserId(), order.getLockedDeposit());
    }

    @Override
    protected OrderQueueManagerPool orderQueueManagerPool() {
        return orderQueueManagerPool;
    }
}
