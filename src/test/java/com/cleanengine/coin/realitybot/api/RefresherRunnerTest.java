package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.order.adapter.out.persistentce.asset.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefresherRunnerTest {

    @Spy
    @InjectMocks
    private UnitPriceRefresher unitPriceRefresher;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private ApplicationArguments applicationArguments;

    @BeforeEach
    public void setUp(){
        when(assetRepository.findAll()).thenReturn(Collections.emptyList());
    }

    @DisplayName("어플리케이션 실행 시 호가 단위 수집")
    @Test
    public void runwithrefrecher() {
        unitPriceRefresher.run(applicationArguments);

        verify(unitPriceRefresher,times(1)).run(any(ApplicationArguments.class));
        verify(unitPriceRefresher,times(1)).initializeUnitPrices();
    }

//    @DisplayName(" ")
}
