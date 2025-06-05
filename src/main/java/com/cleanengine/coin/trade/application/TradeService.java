package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.common.error.BusinessException;
import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.BuyOrderRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.SellOrderRepository;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TradeService {

    private final TradeRepository tradeRepository;
    private final BuyOrderRepository buyOrderRepository;
    private final SellOrderRepository sellOrderRepository;

    public Trade save(Trade trade) {
        return tradeRepository.save(trade);
    }

    @Transactional
    public Order updateOrder(Order order){
        if (order instanceof BuyOrder) {
            return buyOrderRepository.save((BuyOrder) order);
        } else if (order instanceof SellOrder) {
            return sellOrderRepository.save((SellOrder) order);
        } else {
            throw new BusinessException("Unsupported order type: " + order.getClass().getName(), ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
