package com.cleanengine.coin.realitybot.parser;

import com.cleanengine.coin.realitybot.dto.Ticks;
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
public class TickParser {
    private static final Gson gson = new Gson();

    public static List<Ticks> parseGson(String json) {
        return gson.fromJson(json, new TypeToken<List<Ticks>>() {}.getType());
    }
    /*
    * TickService의 목적을 분류하여 api 로 받아온 값을 parsing 과 api vwap을 계산하는 로직으로 분류하였습니다.
    * 따라서 processVWAP과 CalculateVWAP은 제거되었으며
    * 해당 클래스는 TickParser로 명칭을 변경하였습니다.
    * */

}
