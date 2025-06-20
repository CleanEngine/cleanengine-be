package com.cleanengine.coin.realitybot.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateRefresher {
    private final NaverAPIClient naverApiClient;
    private volatile double usdToKrw = 1300.0;

    public void exchangeRate(){
        String exchangeJson = naverApiClient.get();
        System.out.println(exchangeJson);
        if (exchangeJson != null && !exchangeJson.isBlank()){
            this.usdToKrw = naverApiClient.parsejson(exchangeJson);
        }
    }

    public double getUsdToKrw(){
        return this.usdToKrw;
    }

}
