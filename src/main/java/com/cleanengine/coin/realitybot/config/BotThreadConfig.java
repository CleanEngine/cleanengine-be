package com.cleanengine.coin.realitybot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class BotThreadConfig {

    // 티커 처리 전용 풀 (상위 작업)
    @Bean(name = "tickerExecutor")
    public Executor tickerThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);  // 티커 개수와 동일
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(0);   // 작업 거부 방지
        executor.setThreadNamePrefix("ticker-thread-");
        executor.initialize();
        return executor;
    }

    // 거래소 처리 전용 풀 (하위 작업)
    @Bean(name = "exchangeExecutor")
    public Executor exchangeThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);  // 티커(10) * 거래소(5) = 50
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(200); // 버스트 트래픽 대비
        executor.setThreadNamePrefix("exchange-thread-");
        executor.initialize();
        return executor;
    }

}
