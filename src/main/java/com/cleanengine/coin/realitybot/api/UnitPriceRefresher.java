package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.order.infra.AssetRepository;
import com.cleanengine.coin.realitybot.dto.OpeningPrice;
import com.cleanengine.coin.realitybot.parser.OpeningPriceParser;
import com.cleanengine.coin.realitybot.parser.TickParser;
import com.cleanengine.coin.realitybot.vo.UnitPricePolicy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnitPriceRefresher {
    private final UnitPricePolicy unitPricePolicy;
    private final AssetRepository assetRepository;
    private final BithumbAPIClient bithumbAPIClient;
    private final OpeningPriceParser openingPriceParser;
    private final Map<String ,Double> unitPriceCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeUnitPrices() {
        List<Asset> tickers = assetRepository.findAll();
        for (Asset ticker : tickers){
            double unitPrice = fetchOpeningPriceFromAPI(ticker.getName());
            System.out.println(unitPrice+"가 작동되었습니다 ===================");
            unitPriceCache.put(ticker.getName(),unitPrice);
        }
    }

//    @Scheduled(cron = "0 * * * * *")
    public void refreshUnitPrices() {
        unitPriceCache.keySet().forEach(ticker -> {
            unitPriceCache.put(ticker, fetchOpeningPriceFromAPI(ticker));
        });

        List<Asset> tickers = assetRepository.findAll();
        for (Asset ticker : tickers){
            double unitPrice = fetchOpeningPriceFromAPI(ticker.getName());
            System.out.println(unitPrice+"가 작동되었습니다 ===================");
            unitPriceCache.put(ticker.getName(),unitPrice);
        }
    }

    private double fetchOpeningPriceFromAPI(String ticker) {
        String rawJson = bithumbAPIClient.getOpeningPirce(ticker); //api raw데이터
        OpeningPrice json = openingPriceParser.parseGson(rawJson); //json을 list로 변환

        return json.getOpeningPrice();
    }
}
