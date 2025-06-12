package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.order.adapter.out.persistentce.asset.AssetRepository;
import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.realitybot.dto.OpeningPrice;
import com.cleanengine.coin.realitybot.parser.OpeningPriceParser;
import com.cleanengine.coin.realitybot.vo.UnitPricePolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnitPriceRefresher {
    private final UnitPricePolicy unitPricePolicy;
    private final AssetRepository assetRepository;
    private final BithumbAPIClient bithumbAPIClient;
    private final OpeningPriceParser openingPriceParser;
    private final Map<String ,Double> unitPriceCache = new ConcurrentHashMap<>();

    public void initializeUnitPrices() {
        List<Asset> tickers = assetRepository.findAll();
        for (Asset ticker : tickers){
            double unitPrice = fetchOpeningPriceFromAPI(ticker.getTicker());
            unitPriceCache.put(ticker.getTicker(),unitPrice);
        }
    }


    private double fetchOpeningPriceFromAPI(String ticker) {
        String rawJson = bithumbAPIClient.getOpeningPrice(ticker); //api raw데이터
        OpeningPrice json = openingPriceParser.parseGson(rawJson); //json을 list로 변환
        double unitprice = unitPricePolicy.getUnitPrice(json.getOpening_price());
        return unitprice;
    }

    public double getUnitPriceByTicker(String ticker){
        return unitPriceCache.get(ticker);
    }
}
