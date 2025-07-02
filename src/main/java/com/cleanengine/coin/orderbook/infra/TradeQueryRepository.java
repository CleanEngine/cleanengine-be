package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.orderbook.dto.ClosingPriceDto;
import com.cleanengine.coin.orderbook.dto.QClosingPriceDto;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.cleanengine.coin.trade.entity.QTrade.trade;

@Component
public class TradeQueryRepository {
    private final JPAQueryFactory queryFactory;

    public TradeQueryRepository(EntityManager entityManager){
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public ClosingPriceDto getYesterdayClosingPrice(String ticker, LocalDate yesterdayDate) {
        LocalDateTime yesterday = yesterdayDate.atStartOfDay();

        return queryFactory
                .select(new QClosingPriceDto(
                        trade.ticker,
                        Expressions.asDate(yesterdayDate).as("baseDate"),
                        trade.price))
                .from(trade)
                .where(
                        trade.ticker.eq(ticker)
                        .and(trade.tradeTime.goe(yesterday))
                        .and(trade.tradeTime.lt(yesterday.plusDays(1))))
                .orderBy(trade.tradeTime.desc(), trade.id.desc())
                .fetchFirst();
    }
}
