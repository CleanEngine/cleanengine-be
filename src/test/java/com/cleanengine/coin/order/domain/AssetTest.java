package com.cleanengine.coin.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssetTest {
    @Nested
    @DisplayName("Asset 생성 테스트")
    class CreateAssetTest{
        @DisplayName("ticker가 null인 Asset을 생성시 Exception을 반환한다.")
        @Test
        void createAssetWithNullTicker_returnsException() {
            String nullTicker = null;

            assertThrows(IllegalArgumentException.class, () -> new Asset(nullTicker, "name"));
        }

        @DisplayName("name이 null인 Asset을 생성시 Exception을 반환한다.")
        @Test
        void createAssetWithNullName_returnsException() {
            String nullName = null;

            assertThrows(IllegalArgumentException.class, () -> new Asset("BTC", nullName));
        }
        @DisplayName("ticker와 name이 null이 아닌 Asset 생성시 제대로 초기화된다.")
        @Test
        void createAsset_initializedAsExpected() {
            String ticker = "BTC";
            String name = "비트코인";

            Asset asset = new Asset(ticker, name, null);

            assertEquals(ticker, asset.getTicker());
            assertEquals(name, asset.getName());
        }
    }
}
