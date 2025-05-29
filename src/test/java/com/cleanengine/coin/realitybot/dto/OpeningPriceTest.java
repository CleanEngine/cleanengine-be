package com.cleanengine.coin.realitybot.dto;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpeningPriceTest {

    OpeningPrice openingPrice = new OpeningPrice();

    @Test
    void testGsonParsing(){
        String json = "{\n" +
                "    \"market\": \"KRW-BTC\",\n" +
                "    \"trade_date\": \"20180418\",\n" +
                "    \"trade_time\": \"102340\",\n" +
                "    \"trade_date_kst\": \"20180418\",\n" +
                "    \"trade_time_kst\": \"192340\",\n" +
                "    \"trade_timestamp\": 1524047020000,\n" +
                "    \"opening_price\": 8450000,\n" +
                "    \"high_price\": 8679000,\n" +
                "    \"low_price\": 8445000,\n" +
                "    \"trade_price\": 8621000,\n" +
                "    \"prev_closing_price\": 8450000,\n" +
                "    \"change\": \"RISE\",\n" +
                "    \"change_price\": 171000,\n" +
                "    \"change_rate\": 0.0202366864,\n" +
                "    \"signed_change_price\": 171000,\n" +
                "    \"signed_change_rate\": 0.0202366864,\n" +
                "    \"trade_volume\": 0.02467802,\n" +
                "    \"acc_trade_price\": 108024804862.58253,\n" +
                "    \"acc_trade_price_24h\": 232702901371.09308,\n" +
                "    \"acc_trade_volume\": 12603.53386105,\n" +
                "    \"acc_trade_volume_24h\": 27181.31137002,\n" +
                "    \"highest_52_week_price\": 28885000,\n" +
                "    \"highest_52_week_date\": \"2018-01-06\",\n" +
                "    \"lowest_52_week_price\": 4175000,\n" +
                "    \"lowest_52_week_date\": \"2017-09-25\",\n" +
                "    \"timestamp\": 1524047026072\n" +
                "  }";

        Gson gson = new Gson();
        openingPrice = gson.fromJson(json, OpeningPrice.class);
        String actual = openingPrice.toString();
        String expected = "OpeningPrice{market='KRW-BTC', OpeningPrice=8450000.0, tradePrice=8621000.0}";

        assertEquals("KRW-BTC", openingPrice.getMarket());
        assertEquals(8450000, openingPrice.getOpening_price());
        assertEquals(8621000,openingPrice.getTrade_price());

        assertEquals(expected, actual);
    }

}