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
public class CoinoneAPIClient {
    private final OkHttpClient client;
    private String ticker;


    public String get(String ticker){ //API를 responseBody에 담아 반환
        this.ticker = ticker;
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
            log.info("{}의 Bithumb API 응답 : {}",ticker,responseBody);
            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
/*    public String getOpeningPrice(String ticker){
        this.ticker = ticker;
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
