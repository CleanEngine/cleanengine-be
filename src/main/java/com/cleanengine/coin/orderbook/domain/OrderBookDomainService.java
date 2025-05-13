package com.cleanengine.coin.orderbook.domain;

import com.cleanengine.coin.common.error.DomainValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

// TODO DomainService가 아니라 Repository 아닐까?
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

    public List<OrderBookUnit> getBuyOrderBookList(String ticker, int size) {
        return getOrderBook(ticker).getBuyOrderBookList(size);
    }

    public List<OrderBookUnit> getSellOrderBookList(String ticker, int size) {
        return getOrderBook(ticker).getSellOrderBookList(size);
    }

    private OrderBook getOrderBook(String ticker) {
        Optional<OrderBook> orderBook = Optional.ofNullable(orderBookPool.get(ticker));
        if(orderBook.isEmpty()){
            String message = "OrderBook not found. Check ticker sent from client or order.tickers on startup";
            log.debug(message);
            throw new DomainValidationException(message,
                    List.of(new FieldError("OrderBook", "ticker", message)));
        }
        return orderBook.get();
    }
}
