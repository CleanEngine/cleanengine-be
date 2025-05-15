package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.order.application.OrderCommand;
import com.cleanengine.coin.order.application.OrderInfo;
import com.cleanengine.coin.order.application.port.AccountUpdatePort;
import com.cleanengine.coin.order.application.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.domainservice.CreateBuyOrderDomainService;
import com.cleanengine.coin.order.external.adapter.account.AccountExternalRepository;
import com.cleanengine.coin.order.external.adapter.wallet.WalletExternalRepository;
import com.cleanengine.coin.order.infra.BuyOrderRepository;
import com.cleanengine.coin.orderbook.application.service.UpdateOrderBookUsecase;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BuyOrderStrategy extends CreateOrderStrategy<BuyOrder, OrderInfo<BuyOrder>> {
    private final BuyOrderRepository buyOrderRepository;
    private final CreateBuyOrderDomainService createOrderDomainService;
    private final OrderQueueManagerPool orderQueueManagerPool;
    private final AccountUpdatePort accountUpdatePort;
    private final UpdateOrderBookUsecase updateOrderBookUsecase;
    private final WalletExternalRepository walletRepository;
    private final AccountExternalRepository accountRepository;

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
    protected void createWallet(Integer userId, String ticker) {
        if(walletRepository.findWalletBy(userId, ticker).isEmpty()){
            Account account = accountRepository.findByUserId(userId).orElseThrow();
            Wallet wallet = new Wallet(null, ticker, account.getId(), 0.0, 0.0, 0.0);
            walletRepository.save(wallet);
        }
    }

    @Override
    public OrderInfo.BuyOrderInfo extractOrderInfo(Order order) {
        return new OrderInfo.BuyOrderInfo((BuyOrder) order);
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
    protected void updateOrderBook(BuyOrder order) {
        updateOrderBookUsecase.updateOrderBookOnNewOrder(order);
    }
}
