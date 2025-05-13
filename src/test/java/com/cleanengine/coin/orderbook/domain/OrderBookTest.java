package com.cleanengine.coin.orderbook.domain;

import com.cleanengine.coin.order.domain.BuyOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @DisplayName("주문 정보가 삽입되지 않은 상태에서 오더북 요구시 빈 리스트 반환")
    @Test
    void withoutOrder_getOrderBookLists_returnsEmptyList() {
        List<OrderBookUnit> buyOrderBookList = orderBook.getBuyOrderBookList(10);
        List<OrderBookUnit> sellOrderBookList = orderBook.getSellOrderBookList(10);

        assertEquals(0, buyOrderBookList.size());
        assertEquals(0, sellOrderBookList.size());
    }

    @DisplayName("매수주문 정보가 삽입된 상태에서 오더북 요구시 길이가 1인 리스트 반환")
    @Test
    void insertNewBuyOrder_getBuyOrderBookList_returnsSingleSizeList() {
        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);

        List<OrderBookUnit> buyOrderBookList = orderBook.getBuyOrderBookList(10);
        assertEquals(1, buyOrderBookList.size());

        BuyOrderBookUnit buyOrderBookUnit = (BuyOrderBookUnit) buyOrderBookList.get(0);
        assertEquals(10.0, buyOrderBookUnit.getPrice());
        assertEquals(10.0, buyOrderBookUnit.getSize());
    }

    @DisplayName("매수주문 정보가 이미 삽입된 상태에서, 같은 금액의 매수주문 정보 추가후 오더북 요구시 길이가 1인 리스트 반환")
    @Test
    void alreadyBuyOrderInserted_addSamePriceBuyOrder_returnsSingleSizeList(){
        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);

        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);
        List<OrderBookUnit> buyOrderBookList = orderBook.getBuyOrderBookList(10);

        assertEquals(1, buyOrderBookList.size());
        BuyOrderBookUnit buyOrderBookUnit = (BuyOrderBookUnit) buyOrderBookList.get(0);
        assertEquals(20.0, buyOrderBookUnit.getSize());
    }

    @DisplayName("매수주문 정보가 이미 삽입된 상태에서, 다른 금액의 매수주문 정보 추가후 오더북 요구시 길이가 2인 리스트 반환")
    @Test
    void alreadyBuyOrderInserted_addDifferentPriceBuyOrder_returnsTwoSizeList(){
        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);

        orderBook.updateOrderBookOnNewOrder(true, 20.0, 10.0);
        List<OrderBookUnit> buyOrderBookList = orderBook.getBuyOrderBookList(10);

        assertEquals(2, buyOrderBookList.size());
        BuyOrderBookUnit buyOrderBookUnit = (BuyOrderBookUnit) buyOrderBookList.get(0);
        assertEquals(20.0, buyOrderBookUnit.getPrice());
        buyOrderBookUnit = (BuyOrderBookUnit) buyOrderBookList.get(1);
        assertEquals(10.0, buyOrderBookUnit.getPrice());
    }

    @DisplayName("매수주문 정보가 5개 삽입된 상태에서, 길이가 3인 오더북을 요구시, 가장 금액이 큰 3 가격 정보가 반환된다")
    @Test
    void insertedFiveBuyOrders_getBuyOrderBookListWithSizeThree_returnsThreeExpensivePrices(){
        List<BuyOrder> buyOrders = createLimitBuyOrdersWithPrices(10.0, 20.0, 30.0, 40.0, 50.0);
        buyOrders.forEach(order -> orderBook.updateOrderBookOnNewOrder(true, order.getPrice(), order.getOrderSize()));

        List<OrderBookUnit> buyOrderBookList = orderBook.getBuyOrderBookList(3);
        assertEquals(3, buyOrderBookList.size());
        BuyOrderBookUnit buyOrderBookUnit = (BuyOrderBookUnit) buyOrderBookList.get(0);
        assertEquals(50.0, buyOrderBookUnit.getPrice());
        buyOrderBookUnit = (BuyOrderBookUnit) buyOrderBookList.get(1);
        assertEquals(40.0, buyOrderBookUnit.getPrice());
        buyOrderBookUnit = (BuyOrderBookUnit) buyOrderBookList.get(2);
        assertEquals(30.0, buyOrderBookUnit.getPrice());
    }

    @DisplayName("매수주문 정보가 이미 삽입된 상태에서 가격이 같고 다른 크기의 체결이 이루어진 후 오더북 요구시 길이가 1인 리스트 반환")
    @Test
    void alreadyBuyOrderInserted_executeSamePriceDifferentSize_returnSingleSizeList(){
        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);

        orderBook.updateOrderBookOnTradeExecuted(true, 10.0, 5.0);
        List<OrderBookUnit> buyOrderBookList = orderBook.getBuyOrderBookList(10);

        assertEquals(1, buyOrderBookList.size());
        BuyOrderBookUnit buyOrderBookUnit = (BuyOrderBookUnit) buyOrderBookList.get(0);
        assertEquals(5.0, buyOrderBookUnit.getSize());
    }

    @DisplayName("매수주문 정보가 이미 삽입된 상태에서 가격과 크기가 같은 체결이 이루어진 후 오더북 요구시 빈 리스트 반환")
    @Test
    void alreadyBuyOrderInserted_executeSamePriceSameSize_returnEmptySizeList(){
        orderBook.updateOrderBookOnNewOrder(true, 10.0, 10.0);

        orderBook.updateOrderBookOnTradeExecuted(true, 10.0, 10.0);
        List<OrderBookUnit> buyOrderBookList = orderBook.getBuyOrderBookList(10);

        assertEquals(0, buyOrderBookList.size());
    }
}