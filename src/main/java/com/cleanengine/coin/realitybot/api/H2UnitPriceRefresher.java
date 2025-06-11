package com.cleanengine.coin.realitybot.api;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("h2-mem")
@RequiredArgsConstructor
public class H2UnitPriceRefresher implements ApplicationRunner {
    private final UnitPriceRefresher unitPriceRefresher;

    public void run(ApplicationArguments args){
        log.info("Running Unit Price Refresher (h2-mem)...");
        unitPriceRefresher.initializeUnitPrices();
    }

}
