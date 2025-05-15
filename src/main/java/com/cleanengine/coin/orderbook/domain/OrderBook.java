package com.cleanengine.coin.orderbook.domain;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static com.cleanengine.coin.common.CommonValues.approxEquals;

public class OrderBook {
    private final String ticker;
    private final ConcurrentHashMap<Double, BuyOrderBookUnit> buyOrderBookUnitMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Double, SellOrderBookUnit> sellOrderBookUnitMap = new ConcurrentHashMap<>();
    private final ConcurrentSkipListSet<BuyOrderBookUnit> buyOrderBookUnitListSet = new ConcurrentSkipListSet<>();
    private final ConcurrentSkipListSet<SellOrderBookUnit> sellOrderBookUnitListSet = new ConcurrentSkipListSet<>();

    public OrderBook(String ticker) {
        this.ticker = ticker;
    }

    public void updateOrderBookOnNewOrder(boolean isBuyOrder, Double price, Double orderSize) {
        if(isBuyOrder){
            BuyOrderBookUnit buyOrderBookUnit = buyOrderBookUnitMap.get(price);
            if(buyOrderBookUnit == null){
                buyOrderBookUnit = new BuyOrderBookUnit(price, orderSize);
                buyOrderBookUnitMap.put(price, buyOrderBookUnit);
                buyOrderBookUnitListSet.add(buyOrderBookUnit);
            } else {
                buyOrderBookUnit.addOrder(orderSize);
            }
        } else {
            SellOrderBookUnit sellOrderBookUnit = sellOrderBookUnitMap.get(price);
            if(sellOrderBookUnit == null){
                sellOrderBookUnit = new SellOrderBookUnit(price, orderSize);
                sellOrderBookUnitMap.put(price, sellOrderBookUnit);
                sellOrderBookUnitListSet.add(sellOrderBookUnit);
            } else {
                sellOrderBookUnit.addOrder(orderSize);
            }
        }
    }

    public void updateOrderBookOnTradeExecuted(boolean isBuyOrder, Double price, Double orderSize) {
        if(isBuyOrder){
            BuyOrderBookUnit buyOrderBookUnit = buyOrderBookUnitMap.get(price);
            buyOrderBookUnit.executeTrade(orderSize);
            if(approxEquals(buyOrderBookUnit.getSize(), 0.0)){
                buyOrderBookUnitMap.remove(price);
                buyOrderBookUnitListSet.remove(buyOrderBookUnit);
            }
        } else {
            SellOrderBookUnit sellOrderBookUnit = sellOrderBookUnitMap.get(price);
            sellOrderBookUnit.executeTrade(orderSize);
            if(approxEquals(sellOrderBookUnit.getSize(), 0.0)){
                sellOrderBookUnitMap.remove(price);
                sellOrderBookUnitListSet.remove(sellOrderBookUnit);
            }
        }
    }

    public List<OrderBookUnit> getBuyOrderBookList(int size){
        return buyOrderBookUnitListSet
                .stream()
                .limit(size)
                .collect(Collectors.toList());
    }

    public List<OrderBookUnit> getSellOrderBookList(int size){
        return sellOrderBookUnitListSet
                .stream()
                .limit(size)
                .collect(Collectors.toList());
    }
}
