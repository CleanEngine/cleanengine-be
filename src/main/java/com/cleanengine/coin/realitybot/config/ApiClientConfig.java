package com.cleanengine.coin.realitybot.config;

import com.cleanengine.coin.realitybot.api.RetryInterceptor;
import com.cleanengine.coin.realitybot.dto.Ticks;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.Queue;

@Configuration
public class ApiClientConfig {
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(3,50,500))
                .build();
    }
}
