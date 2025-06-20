package com.cleanengine.coin.realitybot.api;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpbitAPIClient implements ExchangesAPIClient {
    private final OkHttpClient client;
    private final MeterRegistry meterRegistry;

    public String get(String ticker){
        Timer timer = Timer.builder("upbit_api_call_duration_seconds")
                .tag("ticker", ticker)
                .tag("status", "200")
                .register(meterRegistry);

        Request request = new Request.Builder()
                .url("https://api.bithumb.com/v1/trades/ticks?market=krw-"+ticker+"&count=10")
                .get()
                .addHeader("accept","application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code()==400) {
                log.warn("DB asset 최신화가 필요합니다 : {}",ticker);
            }
            String responseBody = response.body().string();
            return responseBody;
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getExchangeName() {
        return "Upbit";
    }
}
