package com.cleanengine.coin.realitybot.config;

import com.cleanengine.coin.common.annotation.WorkingServerProfile;
import com.cleanengine.coin.order.application.OrderRestoreService;
import com.cleanengine.coin.realitybot.service.BotOrderCancelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@WorkingServerProfile
@Slf4j
public class BotCancelSchedulerConfig implements SchedulingConfigurer {

    private final BotOrderCancelService botOrderCancelService;

    @Value("${bot-cancel-scheduler.fixed-rate}")
    protected Duration fixedRate;

    @Value("${bot-cancel-scheduler.cancel-rate}")
    protected double cancelRate;

    private boolean restored = false;

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.addFixedRateTask(() -> {
            try {
                if(!restored) return;
                botOrderCancelService.cancelBotOrdersAllTicker(cancelRate);
            } catch (Exception e) {
                log.error("handling되지 않은 에러가 bot 주문취소 스케줄러에서 발생", e);
            }
        }, fixedRate);
    }

    @EventListener
    public void onRestoreCompleted(OrderRestoreService.OrderRestoreCompleted orderRestoreCompleted) {
        restored = true;
    }
}
