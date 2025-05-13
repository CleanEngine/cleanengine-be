package com.cleanengine.coin.orderbook.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class OrderBookDomainService {
    private final HashMap<String, OrderBook> orderBookPool = new HashMap<>();
    @Autowired
    public OrderBookDomainService(@Value("${order.tickers}") String[] tickers) {
        for (String ticker : tickers) {
            orderBookPool.put(ticker, new OrderBook(ticker));
        }
    }

    public void updateOrderBookOnNewOrder(String ticker, boolean isBuyOrder, Double price, Double orderSize) {
        getOrderBook(ticker).updateOrderBookOnNewOrder(isBuyOrder, price, orderSize);
    }

    public void updateOrderBookOnTradeExecuted(String ticker, boolean isBuyOrder, Double price, Double orderSize) {
        getOrderBook(ticker).updateOrderBookOnTradeExecuted(isBuyOrder, price, orderSize);
    }

    public List<OrderPriceInfo> getBuyOrderBookList(String ticker, int size) {
        return getOrderBook(ticker).getBuyOrderBookList(size);
    }

    public List<OrderPriceInfo> getSellOrderBookList(String ticker, int size) {
        return getOrderBook(ticker).getSellOrderBookList(size);
    }

    private OrderBook getOrderBook(String ticker) {
        Optional<OrderBook> orderBook = Optional.ofNullable(orderBookPool.get(ticker));
        if(orderBook.isEmpty()){
            log.debug("OrderBook not found. check order.tickers on startup");
            throw new RuntimeException("OrderBook not found. check order.tickers on startup");
        }
        return orderBook.get();
    }
}
