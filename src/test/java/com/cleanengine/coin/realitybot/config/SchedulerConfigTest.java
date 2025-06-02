package com.cleanengine.coin.realitybot.config;

import com.cleanengine.coin.realitybot.api.ApiScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SchedulerConfigTest {

    @Mock
    ApiScheduler apiScheduler;

    @Mock
    ScheduledTaskRegistrar scheduledTaskRegistrar;

    @InjectMocks
    SchedulerConfig schedulerConfig;

    @BeforeEach
    void setUp() {
        schedulerConfig = new SchedulerConfig(apiScheduler,Duration.ofMillis(500));
    }


    @DisplayName("fixedrate를 적용 후 정상 작동하는 지")
    @Test
    void testConfigureTasksOnFixedRate() throws InterruptedException {
        //when
        schedulerConfig.configureTasks(scheduledTaskRegistrar);

        //then
        ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);//스케줄러에 등록된 작업
        ArgumentCaptor<Duration> intervalCaptor = ArgumentCaptor.forClass(Duration.class);//실행 주기
        //mock에게 전달 된 인자를 캡처해서 확인가능하게 해줌
        //내부 속성을 확인할 때, 동적으로 생성된 값을 검증 할 때

        verify(scheduledTaskRegistrar).addFixedRateTask(taskCaptor.capture(), intervalCaptor.capture());
        //addFixedRateTask를 실행 시 인자 두개를 캡쳐함

        Runnable task = taskCaptor.getValue();
        //그 캡처된 인자중 task는 실제 실행하는 작업 (schedulerconfig에 구현한 것 -> apiScheduler.MarketAllRequest();)
        task.run(); //가져와서 동적으로 실행할 수 있게 됨 -> apiScheduler.MarketAllRequest();

        verify(apiScheduler).MarketAllRequest(); //작동 검증

        Duration interval = intervalCaptor.getValue();
        assertEquals(Duration.ofMillis(500), interval);
    }
    @DisplayName("marketallrequest가 예외 발생 시 에러를 던지는 지 확인")
    @Test
    void testCheckErrorbyMarketallRequest() throws InterruptedException {
        //given
        doThrow(new InterruptedException()).when(apiScheduler).MarketAllRequest();
        //메서드 실행 시 에러 던지도록 셋팅

        //when
        schedulerConfig.configureTasks(scheduledTaskRegistrar);

        //then
        ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);//스케줄러에 등록된 작업
        verify(scheduledTaskRegistrar).addFixedRateTask(taskCaptor.capture(), any(Duration.class));

        Runnable task = taskCaptor.getValue();
        assertThrows(RuntimeException.class, () -> task.run());
    }

}