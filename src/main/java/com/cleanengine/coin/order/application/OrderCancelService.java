package com.cleanengine.coin.order.application;

import com.cleanengine.coin.common.error.UnauthorizedAccessException;
import com.cleanengine.coin.order.adapter.out.persistentce.order.InMemoryUnifiedTickersActiveOrdersManager;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.BuyOrderRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.SellOrderRepository;
import com.cleanengine.coin.order.application.dto.OrderCancelResult;
import com.cleanengine.coin.order.application.port.out.PublishOrderCanceledPort;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.spi.ActiveOrders;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import com.cleanengine.coin.order.domain.spi.WaitingOrdersManager;
import com.cleanengine.coin.orderbook.dto.OrderCanceled;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class OrderCancelService {
    private final InMemoryUnifiedTickersActiveOrdersManager activeOrdersManager;
    private final WaitingOrdersManager waitingOrdersManager;
    private final WalletRepository walletRepository;
    private final AccountRepository accountRepository;
    private final PublishOrderCanceledPort publishOrderCanceledPort;
    private final BuyOrderRepository buyOrderRepository;
    private final SellOrderRepository sellOrderRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderCancelResult cancelOrder(Long orderId, Integer userId){
        if(orderId == null || userId == null) {
            throw new IllegalArgumentException("orderId and userId cannot be null.");
        }

        ActiveOrders activeOrders = activeOrdersManager.getActiveOrders(null);
        ReentrantLock lock = activeOrders.lockOrder(orderId);
        try{
            // 메서드 분할해서 어느 부분에서 에러 발생했는지에 따라 롤백로직 차등 두어야
            Optional<Order> orderOpt = activeOrdersManager.getOrder(orderId);
            if(orderOpt.isEmpty()) throw new IllegalArgumentException("order is not active.");
            Order order = orderOpt.get();

            validateCancel(order, userId);

            refund(order, userId);

            order.setState(OrderStatus.CANCELED);
            saveOrder(order);

            WaitingOrders waitingOrders = waitingOrdersManager.getWaitingOrders(orderOpt.get().getTicker());
            waitingOrders.removeOrder(order);

            activeOrders.removeOrder(orderId);

            publishOrderCanceledPort.publish(new OrderCanceled(order));

            return new OrderCancelResult(order);
        }
        finally {
            if(lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void validateCancel(Order order, Integer userId) {
        if(!order.getUserId().equals(userId)) throw new UnauthorizedAccessException("user is not owner of order.");
        if(order.getState() != OrderStatus.WAIT) throw new IllegalArgumentException("order is not active.");
    }

    private void saveOrder(Order order) {
        if(order instanceof BuyOrder) {
            buyOrderRepository.save((BuyOrder) order);
        }
        else {
            sellOrderRepository.save((SellOrder) order);
        }
    }

    private void refund(Order order, Integer userId) {
        if(order instanceof SellOrder){
            Wallet wallet = walletRepository.findByUserIdAndTicker(userId, order.getTicker()).orElseThrow();
            wallet.increaseSize(order.getRemainingSize());
        } else {
            BuyOrder buyOrder = (BuyOrder) order;
            Account account = accountRepository.findByUserId(userId).orElseThrow();
            account.increaseCash(buyOrder.getRemainingDeposit());
        }
    }

}
