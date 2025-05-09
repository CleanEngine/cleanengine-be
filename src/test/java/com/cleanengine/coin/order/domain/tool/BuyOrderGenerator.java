package com.cleanengine.coin.order.domain.tool;

import com.cleanengine.coin.order.domain.BuyOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public abstract class BuyOrderGenerator {
    private static final String DEFAULT_TICKER = "BTC";
    private static final Integer DEFAULT_USER_ID = 1;
    private static final Double DEFAULT_ORDER_SIZE = 1.0;
    private static final Double DEFAULT_PRICE = 10000.0;
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2025, 5, 9, 10, 18, 0);

    public static class LimitBuyOrderGenerator{
        public static BuyOrder createLimitBuyOrderWithRandomPrice(){
            Random random = new Random();
            return createLimitBuyOrderWithPrice(random.nextDouble() * 100000.0);
        }

        public static BuyOrder createLimitBuyOrderWithPrice(Double price){
            return BuyOrder.createLimitBuyOrder(DEFAULT_TICKER, DEFAULT_USER_ID, DEFAULT_ORDER_SIZE,
                    price, DEFAULT_CREATED_AT, false);
        }

        public static BuyOrder createLimitBuyOrderWithCreatedTime(LocalDateTime createdAt){
            return BuyOrder.createLimitBuyOrder(DEFAULT_TICKER, DEFAULT_USER_ID, DEFAULT_ORDER_SIZE,
                    DEFAULT_PRICE, createdAt, false);
        }

        public static List<BuyOrder> createLimitBuyOrdersWithPrices(Double... prices){
            return Stream.of(prices).map(LimitBuyOrderGenerator::createLimitBuyOrderWithPrice).toList();
        }

        public static List<BuyOrder> createLimitBuyOrdersWithCreatedTimes(LocalDateTime... createdTimes){
            return Stream.of(createdTimes).map(LimitBuyOrderGenerator::createLimitBuyOrderWithCreatedTime).toList();
        }

        public static List<BuyOrder> createLimitBuyOrdersWithDifferentPricesAsc(){
            return createLimitBuyOrdersWithPrices(10000.0, 20000.0, 30000.0, 40000.0);
        }

        public static List<BuyOrder> createLimitBuyOrdersWithDifferentCreatedTimesAsc(){
            LocalDateTime fastestTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
            LocalDateTime secondFastestTime = fastestTime.plusYears(1);
            LocalDateTime thirdFastestTime = secondFastestTime.plusYears(1);
            LocalDateTime slowestTime = thirdFastestTime.plusYears(1);

            return createLimitBuyOrdersWithCreatedTimes(fastestTime, secondFastestTime, thirdFastestTime, slowestTime);
        }
    }

    public static class MarketBuyOrderGenerator{
        public static BuyOrder createMarketBuyOrderWithRandomPrice(){
            Random random = new Random();
            return createMarketBuyOrderWithPrice(random.nextDouble() * 100000.0);
        }

        public static BuyOrder createMarketBuyOrderWithPrice(Double price){
            return BuyOrder.createMarketBuyOrder(DEFAULT_TICKER, DEFAULT_USER_ID,
                    price, DEFAULT_CREATED_AT, false);
        }

        public static BuyOrder createMarketBuyOrderWithCreatedTime(LocalDateTime createdAt){
            return BuyOrder.createMarketBuyOrder(DEFAULT_TICKER, DEFAULT_USER_ID,
                    DEFAULT_PRICE, createdAt, false);
        }

        public static List<BuyOrder> createMarketBuyOrdersWithPrices(Double... prices){
            return Stream.of(prices).map(MarketBuyOrderGenerator::createMarketBuyOrderWithPrice).toList();
        }

        public static List<BuyOrder> createMarketBuyOrdersWithCreatedTimes(LocalDateTime... createdTimes){
            return Stream.of(createdTimes).map(MarketBuyOrderGenerator::createMarketBuyOrderWithCreatedTime).toList();
        }

        public static List<BuyOrder> createMarketBuyOrdersWithDifferentPricesAsc(){
            return createMarketBuyOrdersWithPrices(10000.0, 20000.0, 30000.0, 40000.0);
        }

        public static List<BuyOrder> createMarketBuyOrdersWithDifferentCreatedTimesAsc(){
            LocalDateTime fastestTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
            LocalDateTime secondFastestTime = fastestTime.plusYears(1);
            LocalDateTime thirdFastestTime = secondFastestTime.plusYears(1);
            LocalDateTime slowestTime = thirdFastestTime.plusYears(1);

            return createMarketBuyOrdersWithCreatedTimes(fastestTime, secondFastestTime, thirdFastestTime, slowestTime);
        }
    }
}
