package com.cleanengine.coin.trade.external;

import com.cleanengine.coin.common.error.BusinessException;
import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.trade.TradeBuyOrderRepository;
import com.cleanengine.coin.trade.repository.trade.TradeSellOrderRepository;
import com.cleanengine.coin.trade.repository.trade.TradeTradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TradeTradeService {

    private final TradeTradeRepository tradeTradeRepository;
    private final TradeBuyOrderRepository tradeBuyOrderRepository;
    private final TradeSellOrderRepository tradeSellOrderRepository;

    public Trade save(Trade trade) {
        return tradeTradeRepository.save(trade);
    }

    @Transactional(transactionManager = "tradeTransactionManager")
    public Order updateOrder(Order order){
        if (order instanceof BuyOrder) {
            return tradeBuyOrderRepository.save((BuyOrder) order);
        } else if (order instanceof SellOrder) {
            return tradeSellOrderRepository.save((SellOrder) order);
        } else {
            throw new BusinessException("Unsupported order type: " + order.getClass().getName(), ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
