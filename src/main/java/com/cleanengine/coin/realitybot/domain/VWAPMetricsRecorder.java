package com.cleanengine.coin.realitybot.domain;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class VWAPMetricsRecorder {
    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicReference<Double>> orderPriceMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicReference<Double>> apiVwapMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicReference<Double>> platformVwapMap = new ConcurrentHashMap<>();

//    @Bean
//    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
//        return registry -> registry.config()
//                .commonTags("application", "my-app");  // 모든 메트릭에 자동 추가
//    }


    public void recordPrice(String ticker, boolean isBuy, double price){
        String type = isBuy ? "buy" : "sell";
        String timeStamp = Instant.now().toString();
        String key = ticker +"|"+type+"|"+timeStamp;

        AtomicReference<Double> value = new AtomicReference<>(price);

        meterRegistry.gauge("order_price",
                Tags.of("ticker",ticker,"type",type,"timestamp",timeStamp)
                ,value,AtomicReference::get);
        orderPriceMap.put(key, value);
    }


    public void recordApiVwap(String ticker, double price){
        apiVwapMap.computeIfAbsent(ticker, t -> {
            AtomicReference<Double> ref = new AtomicReference<>(price);
            meterRegistry.gauge("api_vwap",Tags.of("ticker",t),ref,AtomicReference::get);
            return ref;
        }).set(price);
    }
    public void recordPlatformVwap(String ticker, double price){
        platformVwapMap.computeIfAbsent(ticker, t -> {
            AtomicReference<Double> ref = new AtomicReference<>(price);
            meterRegistry.gauge("platform_vwap",Tags.of("ticker",t),ref,AtomicReference::get);
            return ref;
        }).set(price);
    }

}
