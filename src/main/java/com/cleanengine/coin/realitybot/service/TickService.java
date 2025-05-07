package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.dto.Ticks;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TickService {
    private static final Gson gson = new Gson();//이거 왜 static으로? service에서 받아오면 되는데

    public static List<Ticks> paraseGson(String json) {
        return gson.fromJson(json, new TypeToken<List<Ticks>>() {}.getType());
    }
}
