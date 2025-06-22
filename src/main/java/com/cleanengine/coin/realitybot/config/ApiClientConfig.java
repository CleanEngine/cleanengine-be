package com.cleanengine.coin.realitybot.config;

import com.cleanengine.coin.realitybot.api.*;
import com.cleanengine.coin.realitybot.parser.*;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class ApiClientConfig {
    @Bean(name = "defaultClient")
    public OkHttpClient defaultClient() {
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(50,5, TimeUnit.MINUTES))
//                .addInterceptor(new RetryInterceptor(5,50,500))
                .build();
    }

    @Bean(name = "coinbaseClient")
    public OkHttpClient coinbaseClient() {
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
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
