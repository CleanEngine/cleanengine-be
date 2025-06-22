package com.cleanengine.coin.realitybot.api;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
//@RequiredArgsConstructor
@Slf4j
public class CoinbaseAPIClient implements ExchangesAPIClient {
    @Qualifier("coinbaseClient")
    private final OkHttpClient client;
    private final MeterRegistry meterRegistry;

    public CoinbaseAPIClient(@Qualifier("coinbaseClient") OkHttpClient client,MeterRegistry meterRegistry) {
        this.client = client;
        this.meterRegistry = meterRegistry;
    }

    @WithSpan("api.request.01.market.fallback.coinbasecall")
    public String get(String ticker){
        Timer timer = Timer.builder("coinbase_api_call_duration_seconds")
                .tag("ticker",ticker)
                .tag("status","200")
                .register(meterRegistry);

        Request request = new Request.Builder()
                .url("https://api.coinbase.com/api/v3/brokerage/market/products/"+ticker+"-USD/ticker?limit=10")
                .get()
                .addHeader("accept","application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code()==400) {
                log.warn("잘못된 ticker를 입력하였습니다. 입력된 ticker : {}",ticker);
            }
            String responseBody = response.body().string();
            log.debug("{}의 Coinbase API 응답 : {}",ticker,responseBody);
        return responseBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getExchangeName() {
        return "Coinbase";
    }
}
