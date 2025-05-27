package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.order.application.AssetService;
import com.cleanengine.coin.order.application.OrderInfo;
import com.cleanengine.coin.order.application.port.AccountUpdatePort;
import com.cleanengine.coin.order.application.port.out.PublishOrderCreatedPort;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.domainservice.CreateBuyOrderDomainService;
import com.cleanengine.coin.order.domain.domainservice.CreateOrderDomainService;
import com.cleanengine.coin.order.external.adapter.account.AccountExternalRepository;
import com.cleanengine.coin.order.external.adapter.wallet.WalletExternalRepository;
import com.cleanengine.coin.order.infra.BuyOrderRepository;
import com.cleanengine.coin.orderbook.application.service.UpdateOrderBookUsecase;
import org.springframework.stereotype.Component;

@Component
public class BuyOrderStrategy extends CreateOrderStrategy<BuyOrder, OrderInfo<BuyOrder>> {
    private final BuyOrderRepository buyOrderRepository;
    private final AccountUpdatePort accountUpdatePort;
    private final CreateBuyOrderDomainService createOrderDomainService;

    @Override
    public boolean supports(Boolean isBuyOrder) {
        return isBuyOrder;
    }

    @Override
    public void saveOrder(BuyOrder order) {
        buyOrderRepository.save(order);
    }

    @Override
    protected void keepHoldings(BuyOrder order) throws RuntimeException {
        accountUpdatePort.lockDepositForBuyOrder(order.getUserId(), order.getLockedDeposit());
    }

    @Override
    protected CreateOrderDomainService<BuyOrder> createOrderDomainService() {
        return createOrderDomainService;
    }

    @Override
    protected OrderInfo.BuyOrderInfo extractOrderInfo(Order order) {
        return new OrderInfo.BuyOrderInfo((BuyOrder) order);
    }

    public BuyOrderStrategy(PublishOrderCreatedPort publishOrderCreatedPort,
                            AssetService assetService,
                            UpdateOrderBookUsecase updateOrderBookUsecase,
                            WalletExternalRepository walletRepository,
                            AccountExternalRepository accountRepository,
                            BuyOrderRepository buyOrderRepository,
                            AccountUpdatePort accountUpdatePort,
                            CreateBuyOrderDomainService createOrderDomainService) {
        super(publishOrderCreatedPort, assetService, updateOrderBookUsecase, walletRepository, accountRepository);
        this.buyOrderRepository = buyOrderRepository;
        this.accountUpdatePort = accountUpdatePort;
        this.createOrderDomainService = createOrderDomainService;
    }
}
