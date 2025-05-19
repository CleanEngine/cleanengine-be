package com.cleanengine.coin.realitybot.api;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.*;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BithumbAPIClient {
    private OkHttpClient client;
    private Gson gson;

    public String get(){ //API를 responseBody에 담아 반환
        client = new OkHttpClient();
        gson = new Gson();
        Request request = new Request.Builder()
                .url("https://api.bithumb.com/v1/trades/ticks?market=krw-TRUMP&count=10")
                .get()
                .addHeader("accept", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()){
            String responseBody = response.body().string();
//            return gson.toJson(response.body().string());
            log.info("Bithumb API 응답 : {}",responseBody);
            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
