package com.cleanengine.coin.order.domain.tool;

import com.cleanengine.coin.order.domain.SellOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public abstract class SellOrderGenerator {
    private static final String DEFAULT_TICKER = "BTC";
    private static final Integer DEFAULT_USER_ID = 1;
    private static final Double DEFAULT_ORDER_SIZE = 1.0;
    private static final Double DEFAULT_PRICE = 10000.0;
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2025, 5, 9, 10, 18, 0);

    public static class LimitSellOrderGenerator{
        public static SellOrder createLimitSellOrderWithRandomPrice(){
            Random random = new Random();
            return createLimitSellOrderWithPrice(random.nextDouble() * 100000.0);
        }

        public static SellOrder createLimitSellOrderWithPrice(Double price){
            return SellOrder.createLimitSellOrder(DEFAULT_TICKER, DEFAULT_USER_ID, DEFAULT_ORDER_SIZE,
                    price, DEFAULT_CREATED_AT, false);
        }

        public static SellOrder createLimitSellOrderWithCreatedTime(LocalDateTime createdAt){
            return SellOrder.createLimitSellOrder(DEFAULT_TICKER, DEFAULT_USER_ID, DEFAULT_ORDER_SIZE,
                    DEFAULT_PRICE, createdAt, false);
        }

        public static List<SellOrder> createLimitSellOrdersWithPrices(Double... prices){
            return Stream.of(prices).map(SellOrderGenerator.LimitSellOrderGenerator::createLimitSellOrderWithPrice).toList();
        }

        public static List<SellOrder> createLimitSellOrdersWithCreatedTimes(LocalDateTime... createdTimes){
            return Stream.of(createdTimes).map(SellOrderGenerator.LimitSellOrderGenerator::createLimitSellOrderWithCreatedTime).toList();
        }

        public static List<SellOrder> createLimitSellOrdersWithDifferentPricesAsc(){
            return createLimitSellOrdersWithPrices(10000.0, 20000.0, 30000.0, 40000.0);
        }

        public static List<SellOrder> createLimitSellOrdersWithDifferentCreatedTimesAsc(){
            LocalDateTime fastestTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
            LocalDateTime secondFastestTime = fastestTime.plusYears(1);
            LocalDateTime thirdFastestTime = secondFastestTime.plusYears(1);
            LocalDateTime slowestTime = thirdFastestTime.plusYears(1);

            return createLimitSellOrdersWithCreatedTimes(fastestTime, secondFastestTime, thirdFastestTime, slowestTime);
        }
    }

    public static class MarketSellOrderGenerator{
        public static SellOrder createMarketSellOrderWithCreatedTime(LocalDateTime createdAt){
            return SellOrder.createMarketSellOrder(DEFAULT_TICKER, DEFAULT_USER_ID,
                    DEFAULT_ORDER_SIZE, createdAt, false);
        }

        public static List<SellOrder> createMarketSellOrdersWithCreatedTimes(LocalDateTime... createdTimes){
            return Stream.of(createdTimes).map(SellOrderGenerator.MarketSellOrderGenerator::createMarketSellOrderWithCreatedTime).toList();
        }

        public static List<SellOrder> createMarketSellOrdersWithDifferentCreatedTimesAsc(){
            LocalDateTime fastestTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
            LocalDateTime secondFastestTime = fastestTime.plusYears(1);
            LocalDateTime thirdFastestTime = secondFastestTime.plusYears(1);
            LocalDateTime slowestTime = thirdFastestTime.plusYears(1);

            return createMarketSellOrdersWithCreatedTimes(fastestTime, secondFastestTime, thirdFastestTime, slowestTime);
        }
    }
}
