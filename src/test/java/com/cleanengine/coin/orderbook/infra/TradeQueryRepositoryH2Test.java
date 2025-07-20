package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.orderbook.dto.ClosingPriceDto;
import com.cleanengine.coin.trade.application.port.in.TradeQueryUseCase;
import com.cleanengine.coin.trade.application.port.out.TradeQueryRepositoryImpl;
import com.cleanengine.coin.trade.application.service.TradeQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles({"dev", "it", "h2-mem"})
@DataJpaTest
@Import({TradeQueryService.class, TradeQueryRepositoryImpl.class})
public class TradeQueryRepositoryH2Test {
    @Autowired
    private TradeQueryUseCase tradeQueryUseCase;

    @DisplayName("어제 trade가 있었을 경우, 정상적으로 yesterdayClosingPrice를 조회한다.")
    @Test
    @Transactional
    @SqlGroup({
            @Sql(scripts = "classpath:com/cleanengine/coin/orderbook/infra/TradeQueryRepository/insertYesterdayTrade.sql",
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "classpath:com/cleanengine/coin/orderbook/infra/TradeQueryRepository/clearTrade.sql",
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    public void queryYesterdayClosingPriceWithTradeExecutedYesterday_shouldReturnSuccessfully() {
        LocalDate yesterdayDate = LocalDate.of(2025, 7, 1);

        ClosingPriceDto closingPriceDto = tradeQueryUseCase.getYesterdayClosingPrice("BTC", yesterdayDate);

        assertNotNull(closingPriceDto);
        assertEquals("BTC", closingPriceDto.ticker());
        assertEquals(yesterdayDate, closingPriceDto.baseDate());
        assertEquals(400.0, closingPriceDto.closingPrice());
    }

    @DisplayName("어제 trade가 없었을 경우, null을 조회한다.")
    @Test
    @Transactional
    public void queryYesterdayClosingPriceWithoutYesterdayTrade_shouldReturnNull() {
        LocalDate yesterdayDate = LocalDate.of(2025, 7, 1);

        ClosingPriceDto closingPriceDto = tradeQueryUseCase.getYesterdayClosingPrice("BTC", yesterdayDate);

        assertNull(closingPriceDto);
    }

    @DisplayName("어제 trade가 없고, 오늘 00시 00분 00초의 trade가 있었을 때, null을 조회한다.")
    @Test
    @Transactional
    @SqlGroup({
            @Sql(scripts = "classpath:com/cleanengine/coin/orderbook/infra/TradeQueryRepository/insertStartOfTodayTrade.sql",
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "classpath:com/cleanengine/coin/orderbook/infra/TradeQueryRepository/clearTrade.sql",
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    public void queryYesterdayClosingPriceWithTradeExecutedToday_shouldReturnNull() {
        LocalDate yesterdayDate = LocalDate.of(2025, 7, 1);

        ClosingPriceDto closingPriceDto = tradeQueryUseCase.getYesterdayClosingPrice("BTC", yesterdayDate);

        assertNull(closingPriceDto);
    }

    @DisplayName("어제 같은 시간에 여러건의 trade가 있었을 때, id가 가장 큰 ClosingPrice를 조회한다.")
    @Test
    @Transactional
    @SqlGroup({
            @Sql(scripts = "classpath:com/cleanengine/coin/orderbook/infra/TradeQueryRepository/insertDuplicateTimeYesterdayTrade.sql",
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "classpath:com/cleanengine/coin/orderbook/infra/TradeQueryRepository/clearTrade.sql",
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    public void queryYesterdayClosingPriceWithDuplicateTimeTrades_shouldReturnBiggestIdDto() {
        LocalDate yesterdayDate = LocalDate.of(2025, 7, 1);

        ClosingPriceDto closingPriceDto = tradeQueryUseCase.getYesterdayClosingPrice("BTC", yesterdayDate);

        assertNotNull(closingPriceDto);
        assertEquals("BTC", closingPriceDto.ticker());
        assertEquals(yesterdayDate, closingPriceDto.baseDate());
        assertEquals(600.0, closingPriceDto.closingPrice());
    }
}
