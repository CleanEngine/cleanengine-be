package com.cleanengine.coin.order.application;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.order.application.dto.AssetInfo;
import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.order.adapter.out.persistentce.asset.AssetCacheRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.asset.AssetRepository;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;
    private final AssetCacheRepository assetCacheRepository;
    private final TradeRepository tradeRepository;

    private final ConcurrentHashMap<String, Double> currentPriceCache = new ConcurrentHashMap<>();

    public AssetInfo getAssetInfo(String ticker){
        Optional<Asset> assetOpt = getAsset(ticker);
        if(assetOpt.isEmpty()){
           throw new DomainValidationException(
                   String.format("Asset %s not found", ticker),
                   List.of(new FieldError("Asset", "ticker", "Asset not found")));
        }

        return AssetInfo.from(assetOpt.get());
    }

    public List<AssetInfo> getAllAssetInfos(){
        return assetRepository.findAll().stream().map(AssetInfo::from).toList();
    }

    public boolean isAssetExist(String ticker){
        if(assetCacheRepository.isAssetExists(ticker)) return true;

        Optional<Asset> asset = getAsset(ticker);
        asset.ifPresent(assetCacheRepository::saveAsset);

        return asset.isPresent();
    }

    protected Optional<Asset> getAsset(String ticker){
        return assetRepository.findById(ticker);
    }

    public Double getCurrentPrice(String ticker) {
        return currentPriceCache.computeIfAbsent(ticker, t -> {
            Trade recentTrade = tradeRepository.findFirstByTickerOrderByTradeTimeDesc(t);
            return recentTrade == null ? null : recentTrade.getPrice();
        });
    }

    public void updateCurrentPrice(String ticker, double price) {
        currentPriceCache.put(ticker, price);
    }

}