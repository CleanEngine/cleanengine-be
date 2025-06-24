package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.order.application.AssetService;
import com.cleanengine.coin.order.application.dto.OrderCommand;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.order.application.event.OrderCreated;
import com.cleanengine.coin.order.application.port.out.PublishOrderCreatedPort;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.domainservice.CreateOrderDomainService;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.threads.VirtualThreadExecutor;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public abstract class CreateOrderStrategy<T extends Order, S extends OrderInfo<?>> {
    protected final PublishOrderCreatedPort publishOrderCreatedPort;
    protected final AssetService assetService;
    protected final WalletRepository walletRepository;
    protected final AccountRepository accountRepository;

    public S processCreatingOrder(OrderCommand.CreateOrder createOrderCommand){
        validateTicker(createOrderCommand.ticker());
        T order = createOrder(createOrderCommand);

        //큐에 넣고, 호출하면 끝(비동기 콜)
        CompletableFuture.runAsync(() -> tradeOpertion.trade(), VirtualThreadExecutor);

        //그다음에 체결이랑 별개로 실행되면 되면

        //구조적으로 무조권 실행되어야하는 로직
        saveOrder(order); //io(비동기로 뺼수있을까?)
        keepHoldings(order); //i/o
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

        return createOrderDomainService().createOrder(
                createOrderCommand.ticker(), createOrderCommand.userId(),
                createOrderCommand.isBuyOrder(), createOrderCommand.isMarketOrder(),
                createOrderCommand.orderSize(), createOrderCommand.price(),
                createOrderCommand.createdAt(), createOrderCommand.isBot());
    }

}
