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
    VWAPErrorInjector vwapErrorInjector;


    @Test
    @DisplayName("enableInjection() 호출 전에는 작동 안한다")
    void doNotingInjection(){
        vwapErrorInjector.injectErrorTrade();
       verify(tradeRepository,never()).save(any());
    }

    @Test
    @DisplayName("호출 후에 fateTrade 삽입")
    void injectOnceAfterEnable(){
        vwapErrorInjector.injectErrorTrade();

        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository,times(1)).save(captor.capture());

        Trade trade = captor.getValue();
        assertEquals("TRUMP",trade.getTicker());
    }
}