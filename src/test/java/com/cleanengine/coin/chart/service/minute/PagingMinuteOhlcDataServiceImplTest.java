package com.cleanengine.coin.chart.service.minute;

import com.cleanengine.coin.base.MariaDBAdapterTest;
import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// 테스트 실행 전 MariaDBAdapterTest 클래스의 @DataJpaTest, @Disabled 어노테이션 주석처리 필요
@DisplayName("차트 페이징 통합테스트")
@Transactional
@SpringBootTest
public class PagingMinuteOhlcDataServiceImplTest extends MariaDBAdapterTest {

    @Autowired
    private PagingMinuteOhlcDataService pagingMinuteOhlcDataService;

    @DisplayName("페이징을 통해 차트 OHLC를 정상적으로 가져온다.")
    @Test
    @Sql("classpath:db/chart/paging_minute_ohlc_data.sql")
    public void getMinuteOhlcData() {
        // given
        String ticker = "TRUMP";
        int count = 1;
        int interval = 1;
        LocalDateTime from = LocalDateTime.of(2025, 6, 20, 12, 3, 0);

        // when
        List<RealTimeOhlcDto> ohlcData = pagingMinuteOhlcDataService.getMinuteOhlcData(ticker, count, interval, from.minusMinutes(1));

        // then
        assertNotNull(ohlcData);
        assertEquals(1, ohlcData.size());

        RealTimeOhlcDto resultDto = ohlcData.getFirst();
        assertEquals("TRUMP", resultDto.getTicker());
        assertEquals(LocalDateTime.of(2025, 6, 20, 12, 2, 0), resultDto.getTimestamp());
        assertEquals(109500.0, resultDto.getOpen());
        assertEquals(110500.0, resultDto.getHigh());
        assertEquals(109300.0, resultDto.getLow());
        assertEquals(110000.0, resultDto.getClose());
        assertEquals(12.541453, resultDto.getVolume(), 0.000001);
    }

}