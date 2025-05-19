package com.cleanengine.coin.chart.repository;


import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RealTimeTradeRepository extends JpaRepository<Trade, Integer> {


    //JPA가 필요한 데이터를 가지고온다
    Trade findFirstByTickerOrderByTradeTimeDesc(String ticker);


    //특정 기간 내의 거래 중 가장 마지막 거래 찾기
    /*
    해당 시간 범위 내에서 특정 종목의 가장 마지막 거래(tradeTime 기준 내림차순 정렬 후 첫 번째)를 가져옵니다.
     */
    Trade findFirstByTickerAndTradeTimeBetweenOrderByTradeTimeDesc(
            String ticker,
            LocalDateTime yesterdayStart,
            LocalDateTime yesterdayEnd);
}
