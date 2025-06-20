package com.cleanengine.coin.realitybot.parser;

import com.cleanengine.coin.realitybot.dto.Ticks;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface ExchangesParser {
    List<Ticks> parseJson(String json,String ticker) throws JsonProcessingException;

}
