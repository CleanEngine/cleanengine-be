package com.cleanengine.coin.orderbook.domain;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static com.cleanengine.coin.common.CommonValues.approxEquals;

public class OrderBook {
    private final String ticker;
    private final ConcurrentSkipListSet<BuyOrderPriceInfo> buyOrderPriceInfoListSet = new ConcurrentSkipListSet<>();
    private final ConcurrentSkipListSet<SellOrderPriceInfo> sellOrderPriceInfoListSet = new ConcurrentSkipListSet<>();
    private final ConcurrentHashMap<Double, BuyOrderBookUnit> buyOrderPriceInfoMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Double, SellOrderBookUnit> sellOrderPriceInfoMap = new ConcurrentHashMap<>();

    public OrderBook(String ticker) {
        this.ticker = ticker;
    }

    public void updateOrderBookOnNewOrder(boolean isBuyOrder, Double price, Double orderSize) {
        if(isBuyOrder){
            BuyOrderPriceInfo buyOrderPriceInfo = buyOrderPriceInfoMap.get(price);
            if(buyOrderPriceInfo == null){
                buyOrderPriceInfo = new BuyOrderPriceInfo(price, orderSize);
                buyOrderPriceInfoMap.put(price, buyOrderPriceInfo);
                buyOrderPriceInfoListSet.add(buyOrderPriceInfo);
            } else {
                buyOrderPriceInfo.addOrder(orderSize);
            }
        } else {
            SellOrderPriceInfo sellOrderPriceInfo = sellOrderPriceInfoMap.get(price);
            if(sellOrderPriceInfo == null){
                sellOrderPriceInfo = new SellOrderPriceInfo(price, orderSize);
                sellOrderPriceInfoMap.put(price, sellOrderPriceInfo);
                sellOrderPriceInfoListSet.add(sellOrderPriceInfo);
            } else {
                sellOrderPriceInfo.addOrder(orderSize);
            }
        }
    }

    public void updateOrderBookOnTradeExecuted(boolean isBuyOrder, Double price, Double orderSize) {
        if(isBuyOrder){
            BuyOrderPriceInfo buyOrderPriceInfo = buyOrderPriceInfoMap.get(price);
            buyOrderPriceInfo.executeTrade(orderSize);
            if(approxEquals(buyOrderPriceInfo.getSize(), 0.0)){
                buyOrderPriceInfoMap.remove(price);
                buyOrderPriceInfoListSet.remove(buyOrderPriceInfo);
            }
        } else {
            SellOrderPriceInfo sellOrderPriceInfo = sellOrderPriceInfoMap.get(price);
            sellOrderPriceInfo.executeTrade(orderSize);
            if(approxEquals(sellOrderPriceInfo.getSize(), 0.0)){
                sellOrderPriceInfoMap.remove(price);
                sellOrderPriceInfoListSet.remove(sellOrderPriceInfo);
            }
        }
    }

    public List<OrderPriceInfo> getBuyOrderBookList(int size){
        return buyOrderPriceInfoListSet
                .stream()
                .limit(size)
                .collect(Collectors.toList());
    }

    public List<OrderPriceInfo> getSellOrderBookList(int size){
        return sellOrderPriceInfoListSet
                .stream()
                .limit(size)
                .collect(Collectors.toList());
    }
}
