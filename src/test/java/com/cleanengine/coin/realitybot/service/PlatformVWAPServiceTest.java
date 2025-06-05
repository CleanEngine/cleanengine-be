package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.domain.PlatformVWAPState;
import com.cleanengine.coin.trade.entity.Trade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlatformVWAPServiceTest {

    @InjectMocks
    private PlatformVWAPService platformVWAPService;

    @Mock
    private PlatformVWAPState platformVwapState;

    @DisplayName("10개 이하일 때, APIVWAP 기준으로 랜덤값이 반환되는 지")
    @Test
    void testCalculateVWAPLessThan10Trades() {
        //give
        String ticker = "BTC";
        List<Trade> trades = List.of(
                new Trade(1, "BTC", LocalDateTime.now(), 2, 1, 10000.0, 10.0), // 100000
                new Trade(2, "BTC", LocalDateTime.now(), 2, 1, 11000.0, 10.0), // 110000
                new Trade(3, "BTC", LocalDateTime.now(), 2, 1, 12000.0, 10.0) // 120000
        );//이게 적용되면 10000원대
        double apiVWAP = 1000.0; //0.1%의 보정값

        //when
        double result = platformVWAPService.calculateVWAPbyTrades(ticker, trades, apiVWAP);

        //than
        assertEquals(apiVWAP, result,1);
        assertTrue(result>=999.0 && result<=1001.0);
    }

    @DisplayName("10개 이상일 때, trades 기준으로 계산되는 지")
    @Test
    void testCalculateVWAPMoreThan10Trades() {
        //given
        String ticker = "BTC";
        List<Trade> trades = List.of(
                new Trade(1, "BTC", LocalDateTime.now(), 2, 1, 10000.0, 10.0), // 100000
                new Trade(2, "BTC", LocalDateTime.now(), 2, 1, 11000.0, 10.0), // 110000
                new Trade(3, "BTC", LocalDateTime.now(), 2, 1, 12000.0, 10.0), // 120000
                new Trade(4, "BTC", LocalDateTime.now(), 2, 1, 13000.0, 10.0), // 130000
                new Trade(5, "BTC", LocalDateTime.now(), 2, 1, 14000.0, 10.0), // 140000
                new Trade(6, "BTC", LocalDateTime.now(), 2, 1, 15000.0, 10.0), // 150000
                new Trade(7, "BTC", LocalDateTime.now(), 2, 1, 16000.0, 10.0), // 160000
                new Trade(8, "BTC", LocalDateTime.now(), 2, 1, 17000.0, 10.0), // 170000
                new Trade(9, "BTC", LocalDateTime.now(), 2, 1, 18000.0, 10.0), // 180000
                new Trade(10, "BTC", LocalDateTime.now(), 2, 1, 19000.0, 10.0), // 190000
                new Trade(11, "BTC", LocalDateTime.now(), 2, 1, 20000.0, 10.0)  // 200000
        );
        double apiVWAP = 1000.0;
        when(platformVwapState.getVWAP()).thenReturn(15000.0);
        platformVWAPService.vwapMap.put(ticker, platformVwapState);
        //when
        double result  = platformVWAPService.calculateVWAPbyTrades(ticker, trades, apiVWAP);

        //then
        verify(platformVwapState).addTrades(trades);
        verify(platformVwapState).getVWAP();
        assertEquals( 15000.0,result);
    }
    //todo generatevwap null확인안함
}