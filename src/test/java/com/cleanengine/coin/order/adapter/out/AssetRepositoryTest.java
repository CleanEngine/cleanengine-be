package com.cleanengine.coin.order.adapter.out;

import com.cleanengine.coin.base.MariaDBAdapterTest;
import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.order.infra.AssetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssetRepositoryTest extends MariaDBAdapterTest {
    @Autowired
    protected AssetRepository assetRepository;

    @Test
    public void saveAsset(){
        assetRepository.save(new Asset("BTC", "비트코인"));
    }

    @Test
    @Sql(scripts = "./AssetRepository/createBTC.sql")
    public void findByTicker(){
        Asset asset = assetRepository.findById("BTC").get();

        assertEquals("비트코인", asset.getName());
    }

    @Test
    @Sql(scripts = "./AssetRepository/createBTC.sql")
    public void findAll(){
        assertEquals(1, assetRepository.findAll().size());
    }
    
    // Repository의 Custom Query 테스트
}
