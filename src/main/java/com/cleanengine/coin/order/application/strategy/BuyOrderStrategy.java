package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.common.idgenerator.LongIdGenerator;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.BuyOrderRepository;
import com.cleanengine.coin.order.application.AssetService;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.order.application.port.out.PublishOrderCreatedPort;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.domainservice.CreateBuyOrderDomainService;
import com.cleanengine.coin.order.domain.domainservice.CreateOrderDomainService;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.List;

@Component
public class BuyOrderStrategy extends CreateOrderStrategy<BuyOrder, OrderInfo<BuyOrder>> {
    private final BuyOrderRepository buyOrderRepository;
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
        Double lockedDeposit = order.getLockedDeposit();

        Account account = accountRepository.findByUserId(order.getUserId())
                .orElseThrow(() -> new DomainValidationException("Account not found",
                        List.of(new FieldError("account", "userId", "user might not exist"))));

        account.decreaseCash(lockedDeposit);

        accountRepository.save(account);
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
                            WalletRepository walletRepository,
                            AccountRepository accountRepository,
                            LongIdGenerator idGenerator,
                            BuyOrderRepository buyOrderRepository,
                            CreateBuyOrderDomainService createOrderDomainService) {
        super(publishOrderCreatedPort, assetService, walletRepository, accountRepository, idGenerator);
        this.buyOrderRepository = buyOrderRepository;
        this.createOrderDomainService = createOrderDomainService;
    }
}
