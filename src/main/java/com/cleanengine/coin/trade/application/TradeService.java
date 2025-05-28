package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.common.error.BusinessException;
import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.order.adapter.out.persistentce.order.queue.OrderQueueManager;
import com.cleanengine.coin.order.adapter.out.persistentce.order.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.BuyOrderRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.SellOrderRepository;
import com.cleanengine.coin.orderbook.application.service.UpdateOrderBookUsecase;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.cleanengine.coin.common.CommonValues.approxEquals;

@Slf4j
@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepository;
    private final BuyOrderRepository buyOrderRepository;
    private final SellOrderRepository sellOrderRepository;
    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    @Getter
    private final OrderQueueManagerPool orderQueueManagerPool;
    private final UpdateOrderBookUsecase updateOrderBookUsecase;
    private final TradeExecutedEventPublisher tradeExecutedEventPublisher;

    // 1초마다 큐 로깅
    private long lastLogTime = 0;
    private static final long LOG_INTERVAL = 1000;

    public TradeService(TradeRepository tradeRepository, BuyOrderRepository buyOrderRepository, SellOrderRepository sellOrderRepository, AccountRepository accountRepository, WalletRepository walletRepository, OrderQueueManagerPool orderQueueManagerPool, UpdateOrderBookUsecase updateOrderBookUsecase, TradeExecutedEventPublisher tradeExecutedEventPublisher) {
        this.tradeRepository = tradeRepository;
        this.buyOrderRepository = buyOrderRepository;
        this.sellOrderRepository = sellOrderRepository;
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;
        this.orderQueueManagerPool = orderQueueManagerPool;
        this.updateOrderBookUsecase = updateOrderBookUsecase;
        this.tradeExecutedEventPublisher = tradeExecutedEventPublisher;
    }

    public Trade saveTrade(Trade trade) {
        return tradeRepository.save(trade);
    }

    public Order saveOrder(Order order) {
        if (order instanceof BuyOrder) {
            return buyOrderRepository.save((BuyOrder) order);
        } else if (order instanceof SellOrder) {
            return sellOrderRepository.save((SellOrder) order);
        } else {
            throw new BusinessException("Unsupported order type: " + order.getClass().getName(), ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void increaseAccountCash(Order order, Double amount) {
        Account account = this.findAccountByUserId(order.getUserId()).orElseThrow();
        accountRepository.save(account.increaseCash(amount));
    }

    public Optional<Account> findAccountByUserId(Integer userId) {
        return accountRepository.findByUserId(userId);
    }

    public void updateWalletAfterTrade(Order order, String ticker, double tradedSize, double totalTradedPrice) {
        if (order instanceof BuyOrder) {
            Wallet buyerWallet = this.findWalletByUserIdAndTicker(order.getUserId(), ticker);
            double updatedBuySize = buyerWallet.getSize() + tradedSize;
            double currentBuyPrice = buyerWallet.getBuyPrice() == null ? 0.0 : buyerWallet.getBuyPrice();
            double updatedBuyPrice = ((currentBuyPrice * buyerWallet.getSize()) + totalTradedPrice) / updatedBuySize;
            buyerWallet.setSize(updatedBuySize);
            buyerWallet.setBuyPrice(updatedBuyPrice);
            // TODO : ROI 계산
            this.saveWallet(buyerWallet);
        } else if (order instanceof SellOrder) {
            // 매도 시에는 평단가 변동 없음
            Wallet sellerWallet = this.findWalletByUserIdAndTicker(order.getUserId(), ticker);
            this.saveWallet(sellerWallet);
        } else {
            throw new BusinessException("Unsupported order type: " + order.getClass().getName(), ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Wallet findWalletByUserIdAndTicker(Integer userId, String ticker) {
        Account account = findAccountByUserId(userId).orElseThrow();
        return walletRepository.findByAccountIdAndTicker(account.getId(), ticker)
                .orElseGet(() -> createNewWallet(account.getId(), ticker));
    }

    public Wallet createNewWallet(Integer accountId, String ticker) {
        Wallet newWallet = new Wallet();
        newWallet.setAccountId(accountId);
        newWallet.setTicker(ticker);
        newWallet.setSize(0.0);
        newWallet.setBuyPrice(0.0);
        newWallet.setRoi(0.0);
        return newWallet;
    }

    public Wallet saveWallet(Wallet Wallet) {
        return walletRepository.save(Wallet);
    }

    public Trade insertNewTrade(String ticker, BuyOrder buyOrder, SellOrder sellOrder, double tradeSize, Double tradePrice) {
        Trade newTrade = new Trade();
        newTrade.setTicker(ticker);
        newTrade.setBuyUserId(buyOrder.getUserId());
        newTrade.setSellUserId(sellOrder.getUserId());
        newTrade.setPrice(tradePrice);
        newTrade.setSize(tradeSize);

        return this.saveTrade(newTrade);
    }

    public void updateCompletedOrderStatus(Order order) {
        order.setState(OrderStatus.DONE);
    }

    private void writeQueueLog(OrderQueueManager orderQueueManager) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime > LOG_INTERVAL) {
            log.debug("주문 큐 - 시장가매도[{}], 지정가매도[{}], 시장가매수[{}], 지정가매수[{}]",
                    orderQueueManager.getMarketSellOrderQueueSize(),
                    orderQueueManager.getLimitSellOrderQueueSize(),
                    orderQueueManager.getMarketBuyOrderQueueSize(),
                    orderQueueManager.getLimitBuyOrderQueueSize());
            lastLogTime = currentTime;
        }
    }

    public void execMatchAndTrade(String ticker) {
        OrderQueueManager orderQueueManager = orderQueueManagerPool.getOrderQueueManager(ticker);
        // TODO : peek() 해온 Order 객체들을 lock -> 체결 도중 취소 방지
        this.matchOrders(orderQueueManager)
                .ifPresent(tradePair -> executeTrade(orderQueueManager, tradePair, ticker));
    }

    private Optional<TradePair<Order, Order>> matchOrders(OrderQueueManager orderQueueManager) {  // 반환값 : 체결여부
        this.writeQueueLog(orderQueueManager);

        TradePair<Order, Order> targetTradePair;

        // 시장가 주문 우선처리
        SellOrder marketSellOrder = orderQueueManager.getMarketSellOrder();
        SellOrder limitSellOrder = orderQueueManager.getLimitSellOrder();
        BuyOrder marketBuyOrder = orderQueueManager.getMarketBuyOrder();
        BuyOrder limitBuyOrder = orderQueueManager.getLimitBuyOrder();

        if (marketSellOrder != null && limitBuyOrder != null) {
            // 1. 시장가 매도 주문, 지정가 매수 주문
            targetTradePair = new TradePair<>(marketSellOrder, limitBuyOrder);
        } else if (marketBuyOrder != null && limitSellOrder != null) {
            // 2. 시장가 매수 주문, 지정가 매도 주문
            targetTradePair = new TradePair<>(marketBuyOrder, limitSellOrder);
        } else {
            // 3. 지정가 주문
            targetTradePair = this.matchBetweenLimitOrders(limitBuyOrder, limitSellOrder);
        }
        return Optional.ofNullable(targetTradePair);
    }

    private TradePair<Order, Order> matchBetweenLimitOrders(BuyOrder limitBuyOrder, SellOrder limitSellOrder) {
        if (limitSellOrder == null || limitBuyOrder == null)
            return null;

        if (this.canMatch(limitBuyOrder, limitSellOrder))
            return new TradePair<>(limitBuyOrder, limitSellOrder);
        else
            return null;
    }

    private boolean canMatch(BuyOrder buyOrder, SellOrder sellOrder) {
        return buyOrder.getPrice() >= sellOrder.getPrice();
    }

    private record TradeUnitPriceAndSize(double tradedSize, double tradedPrice) {
    }

    public void executeTrade(OrderQueueManager orderQueueManager, TradePair<Order, Order> tradePair, String ticker) {
        BuyOrder buyOrder = tradePair.getBuyOrder();
        SellOrder sellOrder = tradePair.getSellOrder();

        double tradedPrice;
        double tradedSize;
        double totalTradedPrice;

        // 체결 단가, 수량 확정
        TradeUnitPriceAndSize tradeUnitPriceAndSize = getTradeUnitPriceAndSize(buyOrder, sellOrder);
        tradedSize = tradeUnitPriceAndSize.tradedSize();
        tradedPrice = tradeUnitPriceAndSize.tradedPrice();
        if (approxEquals(tradedSize, 0.0)) {
            log.debug("체결 중단! 체결 시도 수량 : {}, 매수단가 : {}, 매도단가 : {}", tradedSize, buyOrder.getPrice(), sellOrder.getPrice());
            return;
        }
        this.writeTradingLog(buyOrder, sellOrder);

        totalTradedPrice = tradedPrice * tradedSize;
        // 주문 잔여수량, 잔여금액 감소
        if (isMarketOrder(buyOrder))
            buyOrder.decreaseRemainingDeposit(totalTradedPrice);
        else
            buyOrder.decreaseRemainingSize(tradedSize);
        sellOrder.decreaseRemainingSize(tradedSize);

        // 주문 완전체결 처리(잔여금액 or 잔여수량이 0)
        this.removeCompletedBuyOrder(orderQueueManager, buyOrder);
        this.removeCompletedSellOrder(orderQueueManager, sellOrder);

        // DB 테이블 저장에 걸리는 시간 측정용
        long beforeTime = System.currentTimeMillis();
        this.saveOrder(buyOrder);
        this.saveOrder(sellOrder);
        long afterTime = System.currentTimeMillis();
        log.debug("주문 테이블에 update하는 데 걸린 시간 : {}ms", afterTime - beforeTime);

        // 예수금 처리
        //   - 매수 잔여금액 반환
        if (isMarketOrder(buyOrder)) {
            ; // TODO : 시장가 거래 시 1원 단위 등 작은 금액이 남을 수도 있는데 처리방안
        } else {
            if (buyOrder.getPrice() > tradedPrice) { // 매도 호가보다 높은 가격에 매수를 시도한 경우, 차액 반환
                double totalRefundAmount = (buyOrder.getPrice() - tradedPrice) * tradedSize;
                this.increaseAccountCash(buyOrder, totalRefundAmount);
            }
        }

        //   - 매도 예수금 처리
        this.increaseAccountCash(sellOrder, totalTradedPrice);

        // 지갑 누적계산
        this.updateWalletAfterTrade(buyOrder, ticker, tradedSize, totalTradedPrice);
        this.updateWalletAfterTrade(sellOrder, ticker, tradedSize, totalTradedPrice);

        // 체결내역 저장
        this.insertNewTrade(ticker, buyOrder, sellOrder, tradedSize, tradedPrice);

        // 호가 조회를 위한 Order Service 메서드 호출
        updateOrderBookUsecase.updateOrderBookOnTradeExecuted(ticker, buyOrder.getId(), sellOrder.getId(), tradedSize);

        TradeExecutedEvent tradeExecutedEvent = TradeExecutedEvent.builder().build();
        tradeExecutedEventPublisher.publish(tradeExecutedEvent);
    }

    private static TradeUnitPriceAndSize getTradeUnitPriceAndSize(BuyOrder buyOrder, SellOrder sellOrder) {
        double tradedPrice;
        double tradedSize;
        if (isMarketOrder(buyOrder)) { // 시장가매수-지정가매도
            tradedPrice = sellOrder.getPrice();
            if (buyOrder.getRemainingDeposit() >= tradedPrice * sellOrder.getRemainingSize()) { // 매수 잔여예수금이 매도 잔여량보다 크거나 같은 경우 (매수 부분체결 or 완전체결, 매도 완전체결)
                tradedSize = sellOrder.getRemainingSize();
            } else {
                tradedSize = buyOrder.getRemainingDeposit() / tradedPrice;
            }
        } else if (isMarketOrder(sellOrder)) { // 시장가매도-지정가매수
            tradedPrice = buyOrder.getPrice();
            tradedSize = Math.min(sellOrder.getRemainingSize(), buyOrder.getRemainingSize());
        } else { // 지정가매수-지정가매도
            tradedPrice = getTradedUnitPrice(buyOrder, sellOrder);
            tradedSize = Math.min(buyOrder.getRemainingSize(), sellOrder.getRemainingSize());
        }
        return new TradeUnitPriceAndSize(tradedSize, tradedPrice);
    }

    private static double getTradedUnitPrice(BuyOrder buyOrder, SellOrder sellOrder) {
        // 주문 시간을 비교하여 먼저 들어온 주문의 가격으로 거래
        if (buyOrder.getCreatedAt().isBefore(sellOrder.getCreatedAt())) {
            return buyOrder.getPrice();
        } else {
            return sellOrder.getPrice();
        }
    }

    private void writeTradingLog(BuyOrder buyOrder, SellOrder sellOrder) {
        log.debug("[{}] 체결 확정!  종목: {}, ({}: {}가 {}로 {}만큼 매수주문), ({}: {}가 {}로 {}만큼 매도주문)",
                Thread.currentThread().threadId(),
                buyOrder.getTicker(),
                buyOrder.getId(),
                buyOrder.getUserId(),
                isMarketOrder(buyOrder) ? "시장가" : "지정가(" + buyOrder.getPrice() + "원)",
                buyOrder.getRemainingSize() == null ? buyOrder.getRemainingDeposit() : buyOrder.getRemainingSize(),
                sellOrder.getId(),
                sellOrder.getUserId(),
                isMarketOrder(sellOrder) ? "시장가" : "지정가(" + sellOrder.getPrice() + "원)",
                sellOrder.getRemainingSize());
    }

    private static Boolean isMarketOrder(Order order) {
        return order.getIsMarketOrder();
    }

    private static Boolean isLimitOrder(Order order) {
        return !order.getIsMarketOrder();
    }

    private void removeCompletedBuyOrder(OrderQueueManager orderQueueManager, BuyOrder order) {
        boolean isOrderCompleted = (isMarketOrder(order) && approxEquals(order.getRemainingDeposit(), 0.0)) ||
                (isLimitOrder(order) && approxEquals(order.getRemainingSize(), 0.0));

        if (isOrderCompleted) {
            orderQueueManager.removeOrderFromQueue(order);
            this.updateCompletedOrderStatus(order);
        }
    }

    private void removeCompletedSellOrder(OrderQueueManager orderQueueManager, SellOrder order) {
        boolean isOrderCompleted = approxEquals(order.getRemainingSize(), 0.0);

        if (isOrderCompleted) {
            orderQueueManager.removeOrderFromQueue(order);
            this.updateCompletedOrderStatus(order);
        }
    }

}
