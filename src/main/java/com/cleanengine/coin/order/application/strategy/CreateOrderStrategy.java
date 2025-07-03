package com.cleanengine.coin.order.application.strategy;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.common.idgenerator.LongIdGenerator;
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
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
public abstract class CreateOrderStrategy<T extends Order, S extends OrderInfo<?>> {
    protected final PublishOrderCreatedPort publishOrderCreatedPort;
    protected final AssetService assetService;
    protected final WalletRepository walletRepository;
    protected final AccountRepository accountRepository;
    protected final LongIdGenerator idGenerator;

    public S processCreatingOrder(OrderCommand.CreateOrder createOrderCommand){
        validateTicker(createOrderCommand.ticker());
        T order = createOrder(createOrderCommand);
        saveOrder(order);
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
        long orderId = idGenerator.nextId();
        LocalDateTime createdAt = idGenerator.extractDateTime(orderId);

        return createOrderDomainService().createOrder(
                orderId,
                createOrderCommand.ticker(), createOrderCommand.userId(),
                createOrderCommand.isBuyOrder(), createOrderCommand.isMarketOrder(),
                createOrderCommand.orderSize(), createOrderCommand.price(),
                createdAt, createOrderCommand.isBot());
    }

}
