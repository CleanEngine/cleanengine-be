package com.cleanengine.coin.realitybot.api;

import okhttp3.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class BithumbAPIClientTest {

    @Mock
    private OkHttpClient client;
    @Mock
    private Call call;
    @InjectMocks
    BithumbAPIClient bithumbAPIClient;

    @Test
    void get() {
    }
    private String ticker = "BTC";
    private String json = "{\n" +
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
    private String failJson = "{}";

    @DisplayName("실행시 API의 response에 Opening_price 값이 들어오는 지")
    @Test
    void callOpeningPrice() throws IOException {
        //given
        ResponseBody responseBody = ResponseBody.create(json, MediaType.get("application/json"));
        Request mockrequest = new Request.Builder().url("http://localhost").build();
        Response mockresponse = new Response.Builder()
                .request(mockrequest)
                .protocol(Protocol.HTTP_1_1)
                .code(400)
                .message("OK")
                .body(responseBody)
                .build();

        when(client.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(mockresponse);

        //when
        String response = bithumbAPIClient.getOpeningPrice(ticker);

        //then
        assertTrue(response.contains("opening_price"));
        assertEquals(json, response);
    }
    @DisplayName("ticker가 잘못된 요청이 들어갔을 때  log를 띄우는 지")
    @Test
    void callFailbyWrongTicker() throws IOException {
        //given

        ResponseBody responseBody = ResponseBody.create(failJson, MediaType.get("application/json"));
        Request mockrequest = new Request.Builder().url("http://localhost").build();
        Response mockresponse = new Response.Builder()
                .request(mockrequest)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseBody)
                .build();

        when(client.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(mockresponse);

        //when
        String response = bithumbAPIClient.getOpeningPrice(ticker);

        //then
        assertTrue(response.contains("{}"));
    }

    //무응답도 대응필요함
    @DisplayName("실행시 API의 response가 실패할 경우 에러를 던지는 지")
    @Test
    void callOpeningPriceFails() throws IOException {
        //given
        //when
        when(client.newCall(any())).thenReturn(call);
        when(call.execute()).thenThrow(new IOException("API 요청 중 예외 발생"));

        //then
        assertThrows(RuntimeException.class, () -> bithumbAPIClient.getOpeningPrice(ticker));
    }
}