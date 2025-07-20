package com.cleanengine.coin.trade.application.port.out;

import com.cleanengine.coin.orderbook.dto.ClosingPriceDto;
import com.cleanengine.coin.orderbook.dto.QClosingPriceDto;
import com.cleanengine.coin.trade.adapter.out.persistence.JpaTradeQueryRepository;
import com.cleanengine.coin.trade.domain.model.Trade;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.cleanengine.coin.trade.domain.model.QTrade.trade;

@Repository
public class TradeQueryRepositoryImpl implements TradeQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private final JpaTradeQueryRepository jpaTradeQueryRepository;

    public TradeQueryRepositoryImpl(EntityManager entityManager, JpaTradeQueryRepository jpaTradeQueryRepository) {
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
        this.jpaTradeQueryRepository = jpaTradeQueryRepository;
    }

    @Override
    public List<Trade> findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(String ticker, LocalDateTime startTime, LocalDateTime endTime) {
        return jpaTradeQueryRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(ticker, startTime, endTime);
    }

    @Override
    public List<Trade> findByBuyUserIdAndTicker(Integer buyUserId, String ticker) {
        return jpaTradeQueryRepository.findByBuyUserIdAndTicker(buyUserId, ticker);
    }

    @Override
    public List<Trade> findBySellUserIdAndTicker(Integer sellUserId, String ticker) {
        return jpaTradeQueryRepository.findBySellUserIdAndTicker(sellUserId, ticker);
    }

    @Override
    public List<Trade> findTop10ByTickerOrderByTradeTimeDesc(String ticker) {
        return jpaTradeQueryRepository.findTop10ByTickerOrderByTradeTimeDesc(ticker);
    }

    @Override
    public List<Trade> findByTickerAndTradeTimeGreaterThanEqualOrderByTradeTimeDesc(String ticker, LocalDateTime lastTime) {
        return jpaTradeQueryRepository.findByTickerAndTradeTimeGreaterThanEqualOrderByTradeTimeDesc(ticker, lastTime);
    }

    @Override
    public Trade findFirstByTickerOrderByTradeTimeDesc(String ticker) {
        return jpaTradeQueryRepository.findFirstByTickerOrderByTradeTimeDesc(ticker);
    }

    @Override
    public ClosingPriceDto getYesterdayClosingPrice(String ticker, LocalDate yesterdayDate) {
        LocalDateTime yesterday = yesterdayDate.atStartOfDay();

        return jpaQueryFactory
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

    @Override
    public long count() {
        return jpaTradeQueryRepository.count();
    }

}
