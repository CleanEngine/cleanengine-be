package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.order.adapter.out.persistentce.asset.AssetRepository;
import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.realitybot.dto.OpeningPrice;
import com.cleanengine.coin.realitybot.parser.OpeningPriceParser;
import com.cleanengine.coin.realitybot.vo.UnitPricePolicy;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
