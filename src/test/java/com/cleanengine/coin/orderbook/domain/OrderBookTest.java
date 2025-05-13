package com.cleanengine.coin.orderbook.domain;

import com.cleanengine.coin.order.domain.BuyOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.cleanengine.coin.order.domain.tool.BuyOrderGenerator.LimitBuyOrderGenerator.createLimitBuyOrdersWithPrices;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderBookTest {
    OrderBook orderBook;

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook("BTC");
    }

    @Test
    void withoutOrder_getOrderBookLists_returnsEmptyList() {
        List<OrderPriceInfo> buyOrderBookList = orderBook.getBuyOrderBookList(10);
        List<OrderPriceInfo> sellOrderBookList = orderBook.getSellOrderBookList(10);

        assertEquals(0, buyOrderBookList.size());
        assertEquals(0, sellOrderBookList.size());
    }

    @Test
    void insertNewBuyOrder_getBuyOrderBookList_returnsSingleSizeList() {
        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);

        List<OrderPriceInfo> buyOrderBookList = orderBook.getBuyOrderBookList(10);
        assertEquals(1, buyOrderBookList.size());

        BuyOrderPriceInfo buyOrderPriceInfo = (BuyOrderPriceInfo) buyOrderBookList.get(0);
        assertEquals(10.0, buyOrderPriceInfo.getPrice());
        assertEquals(10.0, buyOrderPriceInfo.getSize());
    }

    @Test
    void alreadyBuyOrderInserted_addSamePriceBuyOrder_returnsSingleSizeList(){
        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);

        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);
        List<OrderPriceInfo> buyOrderBookList = orderBook.getBuyOrderBookList(10);

        assertEquals(1, buyOrderBookList.size());
        BuyOrderPriceInfo buyOrderPriceInfo = (BuyOrderPriceInfo) buyOrderBookList.get(0);
        assertEquals(20.0, buyOrderPriceInfo.getSize());
    }

    @Test
    void alreadyBuyOrderInserted_addDifferentPriceBuyOrder_returnsTwoSizeList(){
        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);

        orderBook.updateOrderBookOnNewOrder(true, 20.0, 10.0);
        List<OrderPriceInfo> buyOrderBookList = orderBook.getBuyOrderBookList(10);

        assertEquals(2, buyOrderBookList.size());
        BuyOrderPriceInfo buyOrderPriceInfo = (BuyOrderPriceInfo) buyOrderBookList.get(0);
        assertEquals(20.0, buyOrderPriceInfo.getPrice());
        buyOrderPriceInfo = (BuyOrderPriceInfo) buyOrderBookList.get(1);
        assertEquals(10.0, buyOrderPriceInfo.getPrice());
    }

    @Test
    void insertedFiveBuyOrders_getBuyOrderBookListWithSizeThree_returnsThreeExpensivePrices(){
        List<BuyOrder> buyOrders = createLimitBuyOrdersWithPrices(10.0, 20.0, 30.0, 40.0, 50.0);
        buyOrders.forEach(order -> orderBook.updateOrderBookOnNewOrder(true, order.getPrice(), order.getOrderSize()));

        List<OrderPriceInfo> buyOrderBookList = orderBook.getBuyOrderBookList(3);
        assertEquals(3, buyOrderBookList.size());
        BuyOrderPriceInfo buyOrderPriceInfo = (BuyOrderPriceInfo) buyOrderBookList.get(0);
        assertEquals(50.0, buyOrderPriceInfo.getPrice());
        buyOrderPriceInfo = (BuyOrderPriceInfo) buyOrderBookList.get(1);
        assertEquals(40.0, buyOrderPriceInfo.getPrice());
        buyOrderPriceInfo = (BuyOrderPriceInfo) buyOrderBookList.get(2);
        assertEquals(30.0, buyOrderPriceInfo.getPrice());
    }

    @Test
    void alreadyBuyOrderInserted_executeSamePriceDifferentSize_returnSingleSizeList(){
        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);

        orderBook.updateOrderBookOnTradeExecuted(true, 10.0, 5.0);
        List<OrderPriceInfo> buyOrderBookList = orderBook.getBuyOrderBookList(10);

        assertEquals(1, buyOrderBookList.size());
        BuyOrderPriceInfo buyOrderPriceInfo = (BuyOrderPriceInfo) buyOrderBookList.get(0);
        assertEquals(5.0, buyOrderPriceInfo.getSize());
    }

    @Test
    void alreadyBuyOrderInserted_executeSamePriceSameSize_returnEmptySizeList(){
        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);

        orderBook.updateOrderBookOnTradeExecuted(true, 10.0, 10.0);
        List<OrderPriceInfo> buyOrderBookList = orderBook.getBuyOrderBookList(10);

        assertEquals(0, buyOrderBookList.size());
    }
}