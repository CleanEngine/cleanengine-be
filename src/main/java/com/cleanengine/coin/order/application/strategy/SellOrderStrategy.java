package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.order.adapter.out.persistentce.account.AccountExternalRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.SellOrderRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.wallet.WalletExternalRepository;
import com.cleanengine.coin.order.application.AssetService;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.order.application.port.out.PublishOrderCreatedPort;
import com.cleanengine.coin.order.application.port.out.WalletUpdatePort;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.domainservice.CreateOrderDomainService;
import com.cleanengine.coin.order.domain.domainservice.CreateSellOrderDomainService;
import org.springframework.stereotype.Component;

@Component
public class SellOrderStrategy extends CreateOrderStrategy<SellOrder, OrderInfo.SellOrderInfo> {
    private final SellOrderRepository sellOrderRepository;
    private final WalletUpdatePort walletUpdatePort;
    private final CreateSellOrderDomainService createOrderDomainService;

    @Override
    public boolean supports(Boolean isBuyOrder) {
        return !isBuyOrder;
    }

    @Override
    public void saveOrder(SellOrder order) {
        sellOrderRepository.save(order);
    }

    @Override
    protected void keepHoldings(SellOrder order) throws RuntimeException {
        walletUpdatePort.lockAssetForSellOrder(order.getUserId(), order.getTicker(), order.getOrderSize());
    }

    @Override
    protected CreateOrderDomainService<SellOrder> createOrderDomainService() {
        return createOrderDomainService;
    }

    @Override
    protected OrderInfo.SellOrderInfo extractOrderInfo(Order order) {
        return new OrderInfo.SellOrderInfo((SellOrder) order);
    }

    public SellOrderStrategy(PublishOrderCreatedPort publishOrderCreatedPort,
                             AssetService assetService,
                             WalletExternalRepository walletRepository,
                             AccountExternalRepository accountRepository,
                             SellOrderRepository sellOrderRepository,
                             WalletUpdatePort walletUpdatePort,
                             CreateSellOrderDomainService createOrderDomainService) {
        super(publishOrderCreatedPort, assetService, walletRepository, accountRepository);
        this.sellOrderRepository = sellOrderRepository;
        this.walletUpdatePort = walletUpdatePort;
        this.createOrderDomainService = createOrderDomainService;
    }
}
