package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VWAPerrorInJectionSchedulerTest {

    @Mock
    TradeRepository tradeRepository;

    @InjectMocks
    VWAPerrorInJectionScheduler vwaPerrorInJectionScheduler;

    @Test
    @DisplayName("enableInjection() 호출 전에는 작동 안한다")
    void doNotingInjection(){
       vwaPerrorInJectionScheduler.injectFakeTrade();
       verify(tradeRepository,never()).save(any());
    }

    @Test
    @DisplayName("호출 후에 fateTrade 삽입")
    void injectOnceAfterEnable(){
        vwaPerrorInJectionScheduler.enableInjection();
        vwaPerrorInJectionScheduler.injectFakeTrade();

        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository,times(1)).save(captor.capture());

        Trade trade = captor.getValue();
        assertEquals("TRUMP",trade.getTicker());
    }
    @Test
    @DisplayName("한 번 삽입 이후 재삽입 되지 않음")
    void onlyOnecInject(){
        vwaPerrorInJectionScheduler.enableInjection();
        verify(tradeRepository, never()).save(any());
        vwaPerrorInJectionScheduler.injectFakeTrade();
        verify(tradeRepository,times(1)).save(any());

        vwaPerrorInJectionScheduler.injectFakeTrade();
        vwaPerrorInJectionScheduler.enableInjection();
        verify(tradeRepository,times(1)).save(any());

    }

}