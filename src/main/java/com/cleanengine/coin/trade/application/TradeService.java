package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.chart.dto.TradeEventDto;
import com.cleanengine.coin.common.error.BusinessException;
import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.infra.BuyOrderRepository;
import com.cleanengine.coin.order.infra.SellOrderRepository;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;
    private final BuyOrderRepository buyOrderRepository;
    private final SellOrderRepository sellOrderRepository;
    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;

    public TradeService(TradeRepository tradeRepository, BuyOrderRepository buyOrderRepository, SellOrderRepository sellOrderRepository, AccountRepository accountRepository, WalletRepository walletRepository) {
        this.tradeRepository = tradeRepository;
        this.buyOrderRepository = buyOrderRepository;
        this.sellOrderRepository = sellOrderRepository;
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;
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

    private Wallet createNewWallet(Integer accountId, String ticker) {
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

}
