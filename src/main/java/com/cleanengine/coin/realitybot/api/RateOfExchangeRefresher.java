package com.cleanengine.coin.realitybot.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateOfExchangeRefresher {
    private final NaverAPIClient naverApiClient;
    private volatile double usdToKrw = 1300.0;
    private volatile double tryToKrw = 34.51;

    public void exchangeRate(){
        String UsdJson = naverApiClient.getUsd();
        if (UsdJson != null && !UsdJson.isBlank()){
            this.usdToKrw = naverApiClient.parsejson(UsdJson);
        }
        String Tryson = naverApiClient.getTry();
        if (Tryson != null && !Tryson.isBlank()){
            this.usdToKrw = naverApiClient.parsejson(Tryson);
        }
    }

    public double getUsdToKrw(){
        return this.usdToKrw;
    }
    public double getTryToKrw(){
        return this.tryToKrw;
    }

}
