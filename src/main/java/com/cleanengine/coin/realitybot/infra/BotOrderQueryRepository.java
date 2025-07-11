package com.cleanengine.coin.realitybot.infra;

import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.realitybot.dto.BotOrderCount;
import com.cleanengine.coin.realitybot.dto.BotOrderInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;
import static com.querydsl.jpa.JPAExpressions.select;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotOrderQueryRepository {
    private final SQLQueryFactory sqlQueryFactory;

    private static final RelationalPathBase<Object> buyOrderTable = new RelationalPathBase<>(Object.class, "buyOrder", null, "buy_orders");
    private static final RelationalPathBase<Object> sellOrderTable = new RelationalPathBase<>(Object.class, "sellOrder", null, "sell_orders");

    NumberPath<Long> buyOrderId = Expressions.numberPath(Long.class, "buy_order_id");
    NumberPath<Long> sellOrderId = Expressions.numberPath(Long.class, "sell_order_id");
    NumberPath<Long> orderId = Expressions.numberPath(Long.class, "order_id");
    NumberPath<Integer> userId = Expressions.numberPath(Integer.class, "user_id");
    StringPath ticker = Expressions.stringPath("ticker");
    NumberPath<Double> price = Expressions.numberPath(Double.class, "price");
    StringPath state = Expressions.stringPath("state");
    BooleanPath isMarketOrder = Expressions.booleanPath("is_marketorder");

    public BotOrderCount countWaitingBotOrdersByTicker(String targetTicker) {
        if(targetTicker == null) {
            throw new IllegalArgumentException("ticker cannot be null");
        }

        SQLQuery<BotOrderCount> query = sqlQueryFactory
                .select(Projections.constructor(BotOrderCount.class,
                        select(buyOrderId.count())
                                .from(buyOrderTable)
                                .where(
                                        userId.eq(BUY_ORDER_BOT_ID)
                                                .and(ticker.eq(targetTicker))
                                                .and(state.eq(OrderStatus.WAIT.name()))
                                                .and(isMarketOrder.eq(false))
                                ),
                        select(sellOrderId.count())
                                .from(sellOrderTable)
                                .where(
                                        userId.eq(SELL_ORDER_BOT_ID)
                                                .and(ticker.eq(targetTicker))
                                                .and(state.eq(OrderStatus.WAIT.name()))
                                                .and(isMarketOrder.eq(false))
                                )
                ));

        return query.fetchOne();
    }

    public List<BotOrderInfo> findWaitingBotOrdersByTickerAndCount(String targetTicker, BotOrderCount botOrderCount) {
        if(targetTicker == null) throw new IllegalArgumentException("ticker cannot be null");
        if(botOrderCount == null) throw new IllegalArgumentException("botOrderCount cannot be null");

        if(botOrderCount.buyOrderCount() == 0 && botOrderCount.sellOrderCount() == 0) {
            return List.of();
        }

        List<SubQueryExpression<BotOrderInfo>> queries = new ArrayList<>();

        if(botOrderCount.buyOrderCount() > 0) {
            queries.add(sqlQueryFactory
                .select(Projections.constructor(BotOrderInfo.class, userId, ticker, buyOrderId.as(orderId)))
                .from(buyOrderTable)
                .where(
                        userId.eq(BUY_ORDER_BOT_ID)
                                .and(ticker.eq(targetTicker))
                                .and(state.eq(OrderStatus.WAIT.name()))
                                .and(isMarketOrder.eq(false))
                )
                .orderBy(price.asc())
                .limit(botOrderCount.buyOrderCount()));
        }

        if(botOrderCount.sellOrderCount() > 0) {
            queries.add(sqlQueryFactory
                .select(Projections.constructor(BotOrderInfo.class, userId, ticker, sellOrderId.as(orderId)))
                .from(sellOrderTable)
                .where(
                        userId.eq(SELL_ORDER_BOT_ID)
                                .and(ticker.eq(targetTicker))
                                .and(state.eq(OrderStatus.WAIT.name()))
                                .and(isMarketOrder.eq(false))
                )
                .orderBy(price.desc())
                .limit(botOrderCount.sellOrderCount()));
        }

        for(SubQueryExpression<BotOrderInfo> query : queries) {
            SQLQuery<BotOrderInfo> sqlQuery = (SQLQuery<BotOrderInfo>) query;
            log.debug("{}", sqlQuery.getSQL().getSQL());
            log.debug("{}", sqlQuery.getSQL().getNullFriendlyBindings().toString());
        }

        return sqlQueryFactory
                .select(Projections.constructor(BotOrderInfo.class, userId, ticker, orderId))
                .from(SQLExpressions.unionAll(queries))
                .fetch();
    }
}
