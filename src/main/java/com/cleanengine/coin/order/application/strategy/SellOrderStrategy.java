package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.order.application.OrderCommand;
import com.cleanengine.coin.order.application.OrderInfo;
import com.cleanengine.coin.order.application.port.WalletUpdatePort;
import com.cleanengine.coin.order.application.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.domainservice.CreateSellOrderDomainService;
import com.cleanengine.coin.order.infra.SellOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellOrderStrategy extends CreateOrderStrategy<SellOrder, OrderInfo.SellOrderInfo> {
    private final SellOrderRepository sellOrderRepository;
    private final CreateSellOrderDomainService createSellOrderDomainService;
    private final OrderQueueManagerPool orderQueueManagerPool;
    private final WalletUpdatePort walletUpdatePort;

    // TODO SELL Order만의 검증 내용
    @Override
    public SellOrder createOrder(OrderCommand.CreateOrder createOrderCommand) {
        return createSellOrderDomainService.createOrder(createOrderCommand.ticker(), createOrderCommand.userId(),
                createOrderCommand.isBuyOrder(), createOrderCommand.isMarketOrder(), createOrderCommand.orderSize(),
                createOrderCommand.price(), createOrderCommand.createdAt(), createOrderCommand.isBot());
    }

    @Override
    public void saveOrder(SellOrder order) {
        sellOrderRepository.save(order);
    }

    @Override
    public OrderInfo.SellOrderInfo extractOrderInfo(SellOrder order) {
        return new OrderInfo.SellOrderInfo(order);
    }

    @Override
    public boolean supports(Boolean isBuyOrder) {
        return !isBuyOrder;
    }

    @Override
    protected void keepHoldings(SellOrder order) throws RuntimeException {
        walletUpdatePort.lockAssetForSellOrder(order.getUserId(), order.getTicker(), order.getOrderSize());
    }

    @Override
    protected OrderQueueManagerPool orderQueueManagerPool() {
        return orderQueueManagerPool;
    }
}
