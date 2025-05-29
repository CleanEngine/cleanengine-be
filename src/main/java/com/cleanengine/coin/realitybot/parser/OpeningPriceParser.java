package com.cleanengine.coin.realitybot.parser;

import com.cleanengine.coin.realitybot.dto.OpeningPrice;
import com.google.gson.Gson;
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
    private final Gson gson = new Gson();

    public OpeningPrice parseGson(String json) {
        List<OpeningPrice> list = gson.fromJson(json, new TypeToken<List<OpeningPrice>>() {}.getType());
        return list.get(0);
    }


}
