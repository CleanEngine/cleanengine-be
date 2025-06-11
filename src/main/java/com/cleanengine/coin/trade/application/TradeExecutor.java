package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.common.error.BusinessException;
import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.application.AccountService;
import com.cleanengine.coin.user.info.application.WalletService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.cleanengine.coin.common.CommonValues.approxEquals;

@Slf4j
@RequiredArgsConstructor
@Component
public class TradeExecutor {

    private final WalletService walletService;
    private final AccountService accountService;
    @Getter
    private final TradeExecutedEventPublisher tradeExecutedEventPublisher;
    private final TradeService tradeService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeTrade(WaitingOrders waitingOrders, TradePair<Order, Order> tradePair, String ticker) {
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
            Order zeroOrder = approxEquals(buyOrder.getRemainingSize(), 0.0) ? buyOrder : sellOrder;
            throw new TradeZeroOrderException(String.format("체결 중단! 체결 시도 수량 : %s, 매수단가 : %s, 매도단가 : %s",
                                                            tradedSize, buyOrder.getPrice(), sellOrder.getPrice()),
                    zeroOrder);
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
        this.removeCompletedBuyOrder(waitingOrders, buyOrder);
        this.removeCompletedSellOrder(waitingOrders, sellOrder);

        tradeService.updateOrder(buyOrder);
        tradeService.updateOrder(sellOrder);

        // 예수금 처리
        //   - 매수 잔여금액 반환
        if (!isMarketOrder(buyOrder) && buyOrder.getPrice() > tradedPrice) {  // 매도 호가보다 높은 가격에 매수를 시도한 경우, 차액 반환
            double totalRefundAmount = (buyOrder.getPrice() - tradedPrice) * tradedSize;
            this.increaseAccountCash(buyOrder, totalRefundAmount);
        }

        //   - 매도 예수금 처리
        this.increaseAccountCash(sellOrder, totalTradedPrice);

        // 지갑 누적계산
        this.updateWalletAfterTrade(buyOrder, ticker, tradedSize, totalTradedPrice);
        this.updateWalletAfterTrade(sellOrder, ticker, tradedSize, totalTradedPrice);

        // 체결내역 저장
        Trade trade = this.insertNewTrade(ticker, buyOrder, sellOrder, tradedSize, tradedPrice);

        TradeExecutedEvent tradeExecutedEvent = TradeExecutedEvent.of(trade, buyOrder.getId(), sellOrder.getId());
        tradeExecutedEventPublisher.publish(tradeExecutedEvent);
    }

    private Account increaseAccountCash(Order order, Double amount) {
        Account account = accountService.findAccountByUserId(order.getUserId()).orElseThrow();
        return accountService.save(account.increaseCash(amount));
    }

    private Wallet updateWalletAfterTrade(Order order, String ticker, double tradedSize, double totalTradedPrice) {
        if (order instanceof BuyOrder) {
            Wallet buyerWallet = walletService.findWalletByUserIdAndTicker(order.getUserId(), ticker);
            double updatedBuySize = buyerWallet.getSize() + tradedSize;
            double currentBuyPrice = buyerWallet.getBuyPrice() == null ? 0.0 : buyerWallet.getBuyPrice();
            double updatedBuyPrice = ((currentBuyPrice * buyerWallet.getSize()) + totalTradedPrice) / updatedBuySize;
            buyerWallet.setSize(updatedBuySize);
            buyerWallet.setBuyPrice(updatedBuyPrice);
            // TODO : ROI 계산
            return walletService.save(buyerWallet);
        } else if (order instanceof SellOrder) {
            // 매도 시에는 평단가 변동 없음
            Wallet sellerWallet = walletService.findWalletByUserIdAndTicker(order.getUserId(), ticker);
            return walletService.save(sellerWallet);
        } else {
            throw new BusinessException("Unsupported order type: " + order.getClass().getName(), ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Trade insertNewTrade(String ticker, BuyOrder buyOrder, SellOrder sellOrder, double tradeSize, Double tradePrice) {
        Trade newTrade = Trade.of(ticker, LocalDateTime.now(), buyOrder.getUserId(), sellOrder.getUserId(), tradePrice, tradeSize);

        return tradeService.save(newTrade);
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

    private record TradeUnitPriceAndSize(double tradedSize, double tradedPrice) {
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

    private void removeCompletedBuyOrder(WaitingOrders waitingOrders, BuyOrder order) {
        boolean isOrderCompleted = (isMarketOrder(order) && approxEquals(order.getRemainingDeposit(), 0.0)) ||
                (isLimitOrder(order) && approxEquals(order.getRemainingSize(), 0.0));

        if (isOrderCompleted) {
            waitingOrders.removeOrder(order);
            this.updateCompletedOrderStatus(order);
        }
    }

    private void removeCompletedSellOrder(WaitingOrders waitingOrders, SellOrder order) {
        boolean isOrderCompleted = approxEquals(order.getRemainingSize(), 0.0);

        if (isOrderCompleted) {
            waitingOrders.removeOrder(order);
            this.updateCompletedOrderStatus(order);
        }
    }

    private void updateCompletedOrderStatus(Order order) {
        order.setState(OrderStatus.DONE);
    }

    private static boolean isMarketOrder(Order order) {
        return order.getIsMarketOrder();
    }

    private static boolean isLimitOrder(Order order) {
        return !order.getIsMarketOrder();
    }

}