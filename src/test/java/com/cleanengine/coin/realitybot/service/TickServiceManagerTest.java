package com.cleanengine.coin.realitybot.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

public class TickServiceManagerTest {
//    @InjectMocks
    private TickServiceManager tickServiceManager = new TickServiceManager();

    @DisplayName("최초 ticker 입력시 not null이여야 함")
    @Test
    void getNewService() {
        //given
        String ticker = "BTC";
        //when
        ApiVWAPService service = tickServiceManager.getService(ticker);
        //then
        assertNotNull(service);
    }
    @DisplayName("같은 ticker일 경우 동일 객체 반환")
    @Test
    void checksDuplication() {
        //given
        String ticker = "BTC";
        //when
        ApiVWAPService service1 = tickServiceManager.getService(ticker);
        ApiVWAPService service2 = tickServiceManager.getService(ticker);
        //then
        assertSame(service1, service2);
    }

    @DisplayName("다른 ticker일 경우 다른 인스턴스 반환")
    @Test
    void checksOthers() {
        //given
        //when
        ApiVWAPService service1 = tickServiceManager.getService("BTC");
        ApiVWAPService service2 = tickServiceManager.getService("TRUMP");
        //then
        assertNotSame(service1, service2);
    }

}