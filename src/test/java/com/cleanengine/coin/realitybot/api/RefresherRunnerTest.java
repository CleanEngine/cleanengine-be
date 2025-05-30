package com.cleanengine.coin.realitybot.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class RefresherRunnerTest {
    @SpyBean
    private UnitPriceRefresher unitPriceRefresher;

    @DisplayName("어플리케이션 실행 시 호가 단위 수집")
    @Test
    public void runwithrefrecher(){
        verify(unitPriceRefresher,times(1)).run(any(ApplicationArguments.class));
        verify(unitPriceRefresher,times(1)).initializeUnitPrices();
    }

//    @DisplayName(" ")
}
