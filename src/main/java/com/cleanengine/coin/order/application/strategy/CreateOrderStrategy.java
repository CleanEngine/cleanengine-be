package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.order.application.AssetService;
import com.cleanengine.coin.order.application.dto.OrderCommand;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.order.application.event.OrderCreated;
import com.cleanengine.coin.order.application.port.out.PublishOrderCreatedPort;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.domainservice.CreateOrderDomainService;
import com.cleanengine.coin.order.adapter.out.persistentce.account.AccountExternalRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.wallet.WalletExternalRepository;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import lombok.AllArgsConstructor;
import org.springframework.validation.FieldError;

import java.util.List;

@AllArgsConstructor
public abstract class CreateOrderStrategy<T extends Order, S extends OrderInfo<?>> {
    protected final PublishOrderCreatedPort publishOrderCreatedPort;
    protected final AssetService assetService;
    protected final WalletExternalRepository walletRepository;
    protected final AccountExternalRepository accountRepository;

    public S processCreatingOrder(OrderCommand.CreateOrder createOrderCommand){
        validateTicker(createOrderCommand.ticker());
        T order = createOrder(createOrderCommand);
        saveOrder(order);
        createWalletIfNeeded(order.getUserId(), order.getTicker());
        keepHoldings(order);
        publishOrderCreatedPort.publish(new OrderCreated(order));
        return extractOrderInfo(order);
    }

    public abstract boolean supports(Boolean isBuyOrder);

    protected abstract void saveOrder(T order);
    protected abstract void keepHoldings(T order) throws RuntimeException;
    protected abstract CreateOrderDomainService<T> createOrderDomainService();
    protected abstract S extractOrderInfo(Order order);

    protected void validateTicker(String ticker){
        if(!assetService.isAssetExist(ticker)){
            throw new DomainValidationException("Asset not supported "+ticker,
                    List.of(new FieldError("BuyOrder", "ticker", "Asset not supported")));
        }
    }

    protected T createOrder(OrderCommand.CreateOrder createOrderCommand){
        T order = createOrderDomainService().createOrder(
                createOrderCommand.ticker(), createOrderCommand.userId(),
                createOrderCommand.isBuyOrder(), createOrderCommand.isMarketOrder(),
                createOrderCommand.orderSize(), createOrderCommand.price(),
                createOrderCommand.createdAt(), createOrderCommand.isBot());

        return order;
    }

    protected void createWalletIfNeeded(Integer userId, String ticker){
        if(walletRepository.findWalletBy(userId, ticker).isEmpty()){
            Account account = accountRepository.findByUserId(userId).orElseThrow();
            Wallet wallet = Wallet.generateEmptyWallet(ticker, account.getId());
            walletRepository.save(wallet);
        }
    }
}
