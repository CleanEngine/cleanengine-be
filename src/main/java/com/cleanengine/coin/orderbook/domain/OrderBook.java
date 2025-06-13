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
                addBuyOrderBookUnit(price, orderSize);
            } else {
                buyOrderBookUnit.addOrder(orderSize);
            }
        } else {
            SellOrderBookUnit sellOrderBookUnit = sellOrderBookUnitMap.get(price);
            if(sellOrderBookUnit == null){
                addSellOrderBookUnit(price, orderSize);
            } else {
                sellOrderBookUnit.addOrder(orderSize);
            }
        }
    }

    public void updateOrderBookOnTradeExecuted(boolean isBuyOrder, Double price, Double orderSize) {
        if(isBuyOrder){
            BuyOrderBookUnit buyOrderBookUnit = buyOrderBookUnitMap.get(price);
            if(buyOrderBookUnit == null) {
                return;
            }
            buyOrderBookUnit.executeTrade(orderSize);
            if(approxEquals(buyOrderBookUnit.getSize(), 0.0)){
                removeBuyOrderBookUnit(buyOrderBookUnit, price);
            }
        } else {
            SellOrderBookUnit sellOrderBookUnit = sellOrderBookUnitMap.get(price);
            if(sellOrderBookUnit == null) {
                return;
            }
            sellOrderBookUnit.executeTrade(orderSize);
            if(approxEquals(sellOrderBookUnit.getSize(), 0.0)){
                removeSellOrderBookUnit(sellOrderBookUnit, price);
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

    protected synchronized void addBuyOrderBookUnit(Double price, Double size) {
        BuyOrderBookUnit buyOrderBookUnit = buyOrderBookUnitMap.get(price);
        if(buyOrderBookUnit != null){
            return;
        }

        buyOrderBookUnit = new BuyOrderBookUnit(price, size);
        buyOrderBookUnitMap.put(price, buyOrderBookUnit);
        buyOrderBookUnitListSet.add(buyOrderBookUnit);
    }

    protected synchronized void addSellOrderBookUnit(Double price, Double size) {
        SellOrderBookUnit sellOrderBookUnit = sellOrderBookUnitMap.get(price);
        if(sellOrderBookUnit != null){
            return;
        }

        sellOrderBookUnit = new SellOrderBookUnit(price, size);
        sellOrderBookUnitMap.put(price, sellOrderBookUnit);
        sellOrderBookUnitListSet.add(sellOrderBookUnit);
    }

    protected synchronized void removeBuyOrderBookUnit(BuyOrderBookUnit buyOrderBookUnit, Double price) {
        if(approxEquals(buyOrderBookUnit.getSize(), 0.0)) {
            buyOrderBookUnitMap.remove(price);
            buyOrderBookUnitListSet.remove(buyOrderBookUnitMap.get(price));
        }
    }

    protected synchronized void removeSellOrderBookUnit(SellOrderBookUnit sellOrderBookUnit, Double price) {
        if(approxEquals(sellOrderBookUnit.getSize(), 0.0)) {
            sellOrderBookUnitMap.remove(price);
            sellOrderBookUnitListSet.remove(sellOrderBookUnitMap.get(price));
        }
    }
}
