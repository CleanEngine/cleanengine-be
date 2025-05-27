package com.cleanengine.coin.realitybot.parser;

import com.cleanengine.coin.realitybot.dto.OpeningPrice;
import com.cleanengine.coin.realitybot.dto.Ticks;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class OpeningPriceParser {
    private static final Gson gson = new Gson();


    public static OpeningPrice parseGson(String json) {
        ApiResponse response = gson.fromJson(json, ApiResponse.class);

        // JSON 내부 구조: data 필드 안의 값을 OpeningPrice로 변환
        Data data = response.getData();

        return OpeningPrice.builder()
                .market(data.getMarket()) // null일 수 있음
                .openingPrice(Double.parseDouble(data.getOpeningPrice()))
                .tradePrice(Double.parseDouble(data.getTradePrice()))
                .build();
    }

    // 전체 응답 구조
    static class ApiResponse {
        private String status;
        private Data data;

        public Data getData() {
            return data;
        }
    }

    // "data" 객체 내부 구조
    static class Data {
        private String market;

        @SerializedName("opening_price")
        private String openingPrice;

        @SerializedName("trade_price")
        private String tradePrice;

        public String getMarket() {
            return market;
        }

        public String getOpeningPrice() {
            return openingPrice;
        }

        public String getTradePrice() {
            return tradePrice;
        }
    }



}
