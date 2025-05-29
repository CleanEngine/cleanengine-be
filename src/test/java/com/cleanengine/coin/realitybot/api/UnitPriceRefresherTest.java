package com.cleanengine.coin.realitybot.api;

import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.order.infra.AssetRepository;
import com.cleanengine.coin.realitybot.dto.OpeningPrice;
import com.cleanengine.coin.realitybot.parser.OpeningPriceParser;
import com.cleanengine.coin.realitybot.vo.UnitPricePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitPriceRefresherTest {
    @InjectMocks
    private UnitPriceRefresher unitPriceRefresher;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private BithumbAPIClient bithumbAPIClient;

    @Mock
    private OpeningPriceParser openingPriceParser;

    @Mock
    private UnitPricePolicy unitPricePolicy;

    @DisplayName("run 시작시 ")
    @Test
    void testRefresherUnitPrice() {
        //given
        String ticker = "BTC";
        Asset btc = new Asset(ticker,"비트코인",null);

        String json = "[{\"market\": \"KRW-BTC\", \"opening_price\": 1000000, \"trade_price\": 1010000}]";
        OpeningPrice parsed = new OpeningPrice();
        parsed.setOpening_price(1000000);

        when(assetRepository.findAll()).thenReturn(List.of(btc));
        when(bithumbAPIClient.getOpeningPrice(ticker)).thenReturn(json);
        when(openingPriceParser.parseGson(json)).thenReturn(parsed);
        when(unitPricePolicy.getUnitPrice(1000000)).thenReturn(100.0);
        //when
        unitPriceRefresher.refreshUnitPrices();

        //then
        double unitPrice = unitPriceRefresher.getUnitPriceByTicker(ticker);
        assertEquals(100.0, unitPrice);
    }

}