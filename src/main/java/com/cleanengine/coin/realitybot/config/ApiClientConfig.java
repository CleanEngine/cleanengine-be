package com.cleanengine.coin.realitybot.config;

import com.cleanengine.coin.realitybot.api.*;
import com.cleanengine.coin.realitybot.parser.*;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class ApiClientConfig {
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(5,50,500))
                .build();
    }

    @Bean
    public List<ExchangesAPIClient> exchangesAPIClientList (BithumbAPIClient bithumb,
                                                            BinanceAPIClient binance,
                                                            CoinoneAPIClient coinone,
                                                            CoinbaseAPIClient coinbase,
                                                            UpbitAPIClient upbit){

        return List.of(bithumb,binance,coinone,coinbase,upbit);
    }

    @Bean
    public Map<String, ExchangesParser> exchangesParserList(TickParser bithumb,
                                                           BinanceParser binance,
                                                            CoinoneParser coinone,
                                                           CoinbaseParser coinbase,
                                                           TickParser upbit){
        return Map.of("Bithumb",bithumb,
                "Binance",binance,
                "Coinone",coinone,
                "Coinbase",coinbase,
                "Upbit",upbit);
    }
}
