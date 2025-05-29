package com.cleanengine.coin.realitybot.api;


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
public class BithumbAPIClient {
    private OkHttpClient client = new OkHttpClient();
    private String ticker;


    public String get(String ticker){ //API를 responseBody에 담아 반환
        this.ticker = ticker;
//        client = new OkHttpClient();
//        gson = new Gson();
        Request request = new Request.Builder()
                .url("https://api.bithumb.com/v1/trades/ticks?market=krw-"+ticker+"&count=10")
                .get()
                .addHeader("accept", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()){
            String responseBody = response.body().string();
//            return gson.toJson(response.body().string());
            log.debug("{}의 Bithumb API 응답 : {}",ticker,responseBody);
            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getOpeningPrice(String ticker){
        this.ticker = ticker;
        Request request = new Request.Builder()
                .url("https://api.bithumb.com/v1/ticker?markets=KRW-"+ticker)
                .get()
                .addHeader("accept", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()){
            String responseBody = response.body().string();
//            return gson.toJson(response.body().string());
            log.debug("{}의 OpeningPirce 응답 : {}",ticker,responseBody);
            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
