package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.order.application.OrderCommand;
import com.cleanengine.coin.order.application.OrderInfo;
import com.cleanengine.coin.order.application.port.WalletUpdatePort;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.domainservice.CreateSellOrderDomainService;
import com.cleanengine.coin.order.external.adapter.account.AccountExternalRepository;
import com.cleanengine.coin.order.external.adapter.wallet.WalletExternalRepository;
import com.cleanengine.coin.order.infra.SellOrderRepository;
import com.cleanengine.coin.orderbook.application.service.UpdateOrderBookUsecase;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellOrderStrategy extends CreateOrderStrategy<SellOrder, OrderInfo.SellOrderInfo> {
    private final SellOrderRepository sellOrderRepository;
    private final CreateSellOrderDomainService createSellOrderDomainService;
    private final WalletUpdatePort walletUpdatePort;
    private final UpdateOrderBookUsecase updateOrderBookUsecase;
    private final WalletExternalRepository walletRepository;
    private final AccountExternalRepository accountRepository;

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
    protected void createWallet(Integer userId, String ticker) {
        if(walletRepository.findWalletBy(userId, ticker).isEmpty()){
            Account account = accountRepository.findByUserId(userId).orElseThrow();
            Wallet wallet = new Wallet(null, ticker, account.getId(), 0.0, 0.0, 0.0);
            walletRepository.save(wallet);
        }
    }

    @Override
    public OrderInfo.SellOrderInfo extractOrderInfo(Order order) {
        return new OrderInfo.SellOrderInfo((SellOrder) order);
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
    protected void updateOrderBook(SellOrder order) {
        updateOrderBookUsecase.updateOrderBookOnNewOrder(order);
    }
}
