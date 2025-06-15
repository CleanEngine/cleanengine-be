package com.cleanengine.coin.realitybot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class BotThreadConfig {

    @Bean(name = "botExecutor")
    public Executor botThreadPoolExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(3);
        executor.setThreadNamePrefix("realityBot-thread-");
        executor.initialize();
        return executor;
    }

}
