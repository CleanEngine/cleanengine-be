package com.cleanengine.coin.chart.repository;

import com.cleanengine.coin.trade.domain.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//jpa의 시간 테스트를 할때


@DataJpaTest
class RealTimeTradeRepositoryTest {

    private LocalDateTime today;
    private LocalDateTime yesterdayStart;
    private LocalDateTime yesterdayEnd;

    @BeforeEach
    void setUp() {

        today = LocalDateTime.now();
        yesterdayStart = today.minusDays(1).withHour(0).withMinute(0).withSecond(0);
        yesterdayEnd = today.minusDays(1).withHour(23).withMinute(59).withSecond(59);
    }


    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RealTimeTradeRepository repository;

    @DisplayName("특정 기간 내 가장 마지막 거래를 올바르게 조회")
    @Test
    public void findFirstByTickerAndTradeTimeBetweenOrderByTradeTimeDesc() throws Exception {
        // given - 네이티브 쿼리로 정확한 시간 설정
        LocalDateTime time1 = yesterdayStart.plusHours(10);
        LocalDateTime time2 = yesterdayStart.plusHours(15);
        LocalDateTime time3 = yesterdayStart.plusHours(20);

        // 직접 SQL로 데이터 삽입
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO trade (ticker, trade_time, buy_user_id, sell_user_id, price, size) VALUES (?, ?, ?, ?, ?, ?)")
                .setParameter(1, "BTC").setParameter(2, time1).setParameter(3, 1).setParameter(4, 2).setParameter(5, 50000.0).setParameter(6, 1.0)
                .executeUpdate();

        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO trade (ticker, trade_time, buy_user_id, sell_user_id, price, size) VALUES (?, ?, ?, ?, ?, ?)")
                .setParameter(1, "BTC").setParameter(2, time2).setParameter(3, 1).setParameter(4, 2).setParameter(5, 52000.0).setParameter(6, 2.0)
                .executeUpdate();

        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO trade (ticker, trade_time, buy_user_id, sell_user_id, price, size) VALUES (?, ?, ?, ?, ?, ?)")
                .setParameter(1, "BTC").setParameter(2, time3).setParameter(3, 1).setParameter(4, 2).setParameter(5, 51500.0).setParameter(6, 3.0)
                .executeUpdate();

        entityManager.flush();

        // when
        Trade result = repository.findFirstByTickerAndTradeTimeBetweenOrderByTradeTimeDesc(
                "BTC", yesterdayStart, yesterdayEnd);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTicker()).isEqualTo("BTC");
        assertThat(result.getPrice()).isEqualTo(51500.0); // 가장 마지막 시간의 거래
    }



}