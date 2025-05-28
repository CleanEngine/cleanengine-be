package com.cleanengine.coin.order.application;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.order.infra.AssetCacheRepository;
import com.cleanengine.coin.order.infra.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;
    private final AssetCacheRepository assetCacheRepository;

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
}
