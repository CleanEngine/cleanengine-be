package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.realitybot.dto.NaverExchangeRateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
//@RequiredArgsConstructor
@Slf4j
public class NaverAPIClient {
    @Qualifier("defaultClient")
    private final OkHttpClient client;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;

    public NaverAPIClient(@Qualifier("defaultClient") OkHttpClient client,MeterRegistry meterRegistry, ObjectMapper objectMapper) {
        this.client = client;
        this.meterRegistry = meterRegistry;
        this.objectMapper = objectMapper;
    }

//    @WithSpan("api.request.01.market.fallback.bithumbcall")
    public String getUsd() {
        Request request = new Request.Builder()
                .url("https://search.naver.com/p/csearch/content/qapirender.nhn?key=calculator&pkid=141&q=%ED%99%98%EC%9C%A8&where=m&u1=keb&u6=standardUnit&u7=0&u3=USD&u4=KRW&u8=down&u2=1")
                .get()
                .addHeader("accept","application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 400) {
                log.warn("네이버 환율 API를 확인하시기 바랍니다.");
            }
            String responseBody = response.body().string();
            log.debug("환율 정보 API 응답 : {}", responseBody);
            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getTry() {
        Request request = new Request.Builder()
                .url("https://search.naver.com/p/csearch/content/qapirender.nhn?key=calculator&pkid=141&q=환율&where=m&u1=keb&u6=standardUnit&u7=0&u3=TRY&u4=KRW&u8=down&u2=1")
                .get()
                .addHeader("accept","application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 400) {
                log.warn("네이버 환율 API를 확인하시기 바랍니다.");
            }
            String responseBody = response.body().string();
            log.debug("환율 정보 API 응답 : {}", responseBody);
            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double parsejson(String json) {
        try {
            NaverExchangeRateResponse response = objectMapper.readValue(json, NaverExchangeRateResponse.class);
            for (NaverExchangeRateResponse.Exchanges ex : response.getCountry()){
                if ("원".equals(ex.getCurrencyUnit())){
                    String cleanedValue = ex.getValue().replace(",", "");
                    return Double.parseDouble(cleanedValue);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
            throw new IllegalStateException("네이버 API 연결 할 수 없습니다.");
    }
}
