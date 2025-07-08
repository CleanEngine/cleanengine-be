package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.cleanengine.coin.order.domain.QBuyOrder.buyOrder;

@Component
public class OrderBookBuyOrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public OrderBookBuyOrderQueryRepository(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public Optional<OrderInfo.BuyOrderInfo> getBuyOrderInfo(long id) {
        return Optional.ofNullable(
                queryFactory
                        .select(Projections.constructor(OrderInfo.BuyOrderInfo.class,
                                buyOrder.id,
                                buyOrder.ticker,
                                buyOrder.state,
                                buyOrder.userId,
                                Expressions.constant(true),
                                buyOrder.isMarketOrder,
                                buyOrder.orderSize,
                                buyOrder.price,
                                buyOrder.createdAt,
                                buyOrder.lockedDeposit,
                                buyOrder.remainingDeposit
                        ))
                        .from(buyOrder)
                        .where(buyOrder.id.eq(id))
                        .fetchOne());
    }
}
