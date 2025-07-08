package com.cleanengine.coin.mypage.service;

import com.cleanengine.coin.mypage.dto.CompletedOrderDto;
import com.cleanengine.coin.mypage.repository.CompletedBuyOrderRepository;
import com.cleanengine.coin.mypage.repository.CompletedOrderRepository;
import com.cleanengine.coin.mypage.repository.CompletedSellOrderRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.asset.AssetRepository;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.trade.entity.Trade;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class CompletedOrderService {
    private final CompletedOrderRepository completedOrderRepository;
    private final CompletedBuyOrderRepository completedBuyOrderRepository;
    private final CompletedSellOrderRepository completedSellOrderRepository;
    private final AssetRepository assetRepository;

    public List<CompletedOrderDto> getCompletedOrders(Integer userId) {
        System.out.println("============ service : "+userId+"==================");
//        List<Trade> trades = completedOrderRepository.findAllByBuyUserIdOrderByTradeTimeAsc(userId);
        List<BuyOrder> buyOrders = completedBuyOrderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        List<SellOrder> sellOrders = completedSellOrderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        List<CompletedOrderDto> buyDtos = buyOrders.stream()
                .map(b ->
                        new CompletedOrderDto(true,b.getState(), b.getId(), b.getTicker(),
                        assetRepository.findByTicker(b.getTicker()).getName(),b.getPrice()
                                ,b.getOrderSize(),b.getCreatedAt())).toList();
        List<CompletedOrderDto> sellDtos = sellOrders.stream()
                .map(s ->
                        new CompletedOrderDto(false,s.getState(), s.getId(), s.getTicker(),
                                assetRepository.findByTicker(s.getTicker()).getName(),s.getPrice()
                                ,s.getOrderSize(),s.getCreatedAt())).toList();
        List<CompletedOrderDto> completedOrderDtos = Stream.concat(buyDtos.stream(),sellDtos.stream())
                .sorted(Comparator.comparing(CompletedOrderDto::getTradeTime).reversed()).toList();
//        System.out.println("============ service : "+trades.get(0).getId()+"==================");
        return completedOrderDtos;
    }

}
