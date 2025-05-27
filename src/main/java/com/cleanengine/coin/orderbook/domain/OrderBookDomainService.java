package com.cleanengine.coin.orderbook.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

// TODO DomainService가 아니라 Repository 아닐까?
@Slf4j
@Component
public class OrderBookDomainService {
    private final HashMap<String, OrderBook> orderBookPool = new HashMap<>();

    public void updateOrderBookOnNewOrder(String ticker, boolean isBuyOrder, Double price, Double orderSize) {
        getOrderBook(ticker).updateOrderBookOnNewOrder(isBuyOrder, price, orderSize);
    }

    public void updateOrderBookOnTradeExecuted(String ticker, boolean isBuyOrder, Double price, Double orderSize) {
        getOrderBook(ticker).updateOrderBookOnTradeExecuted(isBuyOrder, price, orderSize);
    }

    public List<OrderBookUnit> getBuyOrderBookList(String ticker, int size) {
        return getOrderBook(ticker).getBuyOrderBookList(size);
    }

    public List<OrderBookUnit> getSellOrderBookList(String ticker, int size) {
        return getOrderBook(ticker).getSellOrderBookList(size);
    }

    protected OrderBook getOrderBook(String ticker) {
        if(!orderBookPool.containsKey(ticker)){
            addOrderBook(ticker);
        }

        Optional<OrderBook> orderBookOpt = Optional.ofNullable(orderBookPool.get(ticker));
        if(orderBookOpt.isEmpty()){
            log.debug("OrderBook not found. with " + ticker);
            throw new RuntimeException("OrderBook not found with " + ticker);
        }

        return orderBookOpt.get();
    }

    protected synchronized void addOrderBook(String ticker){
        if(!orderBookPool.containsKey(ticker)){
            orderBookPool.put(ticker, new OrderBook(ticker));
        }
    }
}
