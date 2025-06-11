package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.SellOrderRepository;
import com.cleanengine.coin.order.application.AssetService;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.order.application.port.out.PublishOrderCreatedPort;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.domainservice.CreateOrderDomainService;
import com.cleanengine.coin.order.domain.domainservice.CreateSellOrderDomainService;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.List;

@Component
public class SellOrderStrategy extends CreateOrderStrategy<SellOrder, OrderInfo.SellOrderInfo> {
    private final SellOrderRepository sellOrderRepository;
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
        Integer userId = order.getUserId();
        String ticker = order.getTicker();
        Double orderSize = order.getOrderSize();

        Wallet wallet = walletRepository
                .findByAccountIdAndTicker(userId, ticker)
                .orElseThrow(()->
                        new DomainValidationException("Wallet not found",
                                List.of(new FieldError("wallet", "userId", "user might not exist"),
                                        new FieldError("wallet", "ticker", "ticker might be wrong"))));

        wallet.decreaseSize(orderSize);

        walletRepository.save(wallet);
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
                             WalletRepository walletRepository,
                             AccountRepository accountRepository,
                             SellOrderRepository sellOrderRepository,
                             CreateSellOrderDomainService createOrderDomainService) {
        super(publishOrderCreatedPort, assetService, walletRepository, accountRepository);
        this.sellOrderRepository = sellOrderRepository;
        this.createOrderDomainService = createOrderDomainService;
    }
}
