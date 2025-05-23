package com.cleanengine.coin.order.infra;

import com.cleanengine.coin.order.domain.Asset;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AssetCacheRepository {
    private final ConcurrentHashMap<String, Asset> assetCache = new ConcurrentHashMap<>();

    public synchronized void saveAsset(Asset asset) {
        if(!assetCache.containsKey(asset.getTicker())){
            assetCache.put(asset.getTicker(), asset);
        }
    }

    public Optional<Asset> getAsset(String ticker){
        return Optional.ofNullable(assetCache.get(ticker));
    }

    public boolean isAssetExists(String ticker){
        return assetCache.containsKey(ticker);
    }
}
