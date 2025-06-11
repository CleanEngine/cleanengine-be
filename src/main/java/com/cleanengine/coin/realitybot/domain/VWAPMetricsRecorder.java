package com.cleanengine.coin.realitybot.domain;

import io.micrometer.core.instrument.DistributionSummary;
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
    private final ConcurrentHashMap<String, DistributionSummary> orderPriceSummery = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicReference<Double>> apiVwapMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicReference<Double>> platformVwapMap = new ConcurrentHashMap<>();

    public void recordPrice(String ticker, boolean isBuy, double price){
        String type = isBuy ? "buy" : "sell";
        String timeStamp = Instant.now().toString();
//        String key = ticker +"|"+type+"|"+timeStamp;
        String key = ticker +"|"+type;


        AtomicReference<Double> priceRef = orderPriceMap.computeIfAbsent(key, k -> {

            AtomicReference<Double> value = new AtomicReference<>(price);

            meterRegistry.gauge("order_price",
                    Tags.of("ticker",ticker,"type",type)
                    ,value,AtomicReference::get);
            return value;
        });
        priceRef.set(price);

        DistributionSummary summary = orderPriceSummery.computeIfAbsent(key,k ->
                DistributionSummary.builder("order_price_summary")
                        .tags(Tags.of("ticker",ticker,"type",type))
                        .publishPercentiles(0.05,0.95)
                        .register(meterRegistry)
        );
        summary.record(price);
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
