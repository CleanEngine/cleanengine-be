package com.cleanengine.coin.chart.repository;

import com.cleanengine.coin.trade.domain.model.Trade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
class MinuteOhlcDataRepositoryTest {

    @Autowired
    private MinuteOhlcDataRepository repository;

    @DisplayName("db를 ticker로 모든 트레이드를 시간 순으로 조회")
    @Test
    public void findByTickerOrderByTradeTimeAsc() throws Exception {
        //given
        Trade trade1 = new Trade();
        trade1.setTicker("BTC");
        trade1.setSize(1.0);
        trade1.setPrice(10000.0);
        trade1.setTradeTime(LocalDateTime.now());
        trade1.setBuyUserId(Integer.valueOf("1"));
        trade1.setSellUserId(Integer.valueOf("2"));

        Trade trade2 = new Trade();
        trade2.setTicker("BTC");
        trade2.setSize(2.0);
        trade2.setPrice(20000.0);
        trade2.setTradeTime(LocalDateTime.now());
        trade2.setBuyUserId(3);
        trade2.setSellUserId(4);


        Trade trade3 = new Trade();
        trade3.setTicker("BTC");
        trade3.setSize(3.0);
        trade3.setPrice(30000.0);
        trade3.setTradeTime(LocalDateTime.now());
        trade3.setBuyUserId(5);
        trade3.setSellUserId(6);

        repository.saveAll(List.of(trade1, trade2, trade3));


        //when
        List<Trade> result = repository.findByTickerOrderByTradeTimeAsc("BTC");

        //then
        assertThat(result).hasSize(3)
                .extracting("ticker", "size", "price", "TradeTime")
                .containsExactlyInAnyOrder(
                        tuple("BTC", 1.0, 10000.0, trade1.getTradeTime()),
                        tuple("BTC", 2.0, 20000.0, trade2.getTradeTime()),
                        tuple("BTC", 3.0, 30000.0, trade3.getTradeTime())
                );
    }

}