package com.cleanengine.coin.realitybot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NaverExchangeRateResponse {
    private String pkid;
    private String count;
    private List<Exchanges> country;
    private String calculatorMessage;

    @Getter
    @Setter
    public static class Exchanges{
        private String value;
        private String subValue;
        private String currencyUnit;
    }
}
