package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.cleanengine.coin.order.domain.QSellOrder.sellOrder;

@Component
public class OrderBookSellOrderQueryRepository {
    
    private final JPAQueryFactory queryFactory;

    public OrderBookSellOrderQueryRepository(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public Optional<OrderInfo.SellOrderInfo> getSellOrderInfo(long id) {
        return Optional.ofNullable(
                queryFactory
                        .select(Projections.constructor(OrderInfo.SellOrderInfo.class,
                                sellOrder.id,
                                sellOrder.ticker,
                                sellOrder.state,
                                sellOrder.userId,
                                Expressions.constant(false),
                                sellOrder.isMarketOrder,
                                sellOrder.orderSize,
                                sellOrder.price,
                                sellOrder.createdAt
                        ))
                        .from(sellOrder)
                        .where(sellOrder.id.eq(id))
                        .fetchOne());
    }
}
