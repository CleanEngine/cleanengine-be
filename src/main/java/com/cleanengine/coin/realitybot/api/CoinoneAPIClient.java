package com.cleanengine.coin.realitybot.api;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.instrumentation.annotations.WithSpan;
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
public class CoinoneAPIClient {
    private final OkHttpClient client;
    private final MeterRegistry meterRegistry;

    @WithSpan("api.request.01.market.fallback.coinonecall")
    public String get(String ticker){ //API를 responseBody에 담아 반환
        Timer timer = Timer.builder("coinone_api_call_duration_seconds")
                .tag("ticker", ticker)
                .tag("status", "200")
                .register(meterRegistry);

        Request request = new Request.Builder()
                .url("https://api.coinone.co.kr/public/v2/trades/KRW/"+ticker+"?size=10")
                .get()
                .addHeader("accept", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()){
            if ((response.code() == 400)){
                log.warn("잘못된 ticker를 입력하였습니다. 입력된 ticker : {}",ticker);
            }
            String responseBody = response.body().string();
//            return gson.toJson(response.body().string());
            log.debug("{}의 Coinone API 응답 : {}",ticker,responseBody);
            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
/*    public String getOpeningPrice(String ticker){
        Request request = new Request.Builder()
                .url("https://api.bithumb.com/v1/ticker?markets=KRW-"+ticker)
                .get()
                .addHeader("accept", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()){
            if ((response.code() == 400)){
                log.warn("잘못된 ticker를 입력하였습니다. 입력된 ticker : {}",ticker);
            }
            String responseBody = response.body().string();
//            return gson.toJson(response.body().string());
            log.debug("{}의 OpeningPirce 응답 : {}",ticker,responseBody);
            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException("API 요청 중 예외 발생",e);
        }
    }*/

}
