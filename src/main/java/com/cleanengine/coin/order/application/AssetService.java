package com.cleanengine.coin.order.application;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.order.infra.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;

    public AssetInfo getAssetInfo(String ticker){
        Asset asset = assetRepository.findById(ticker).orElseThrow(
                () -> new DomainValidationException(
                        String.format("Asset %s not found", ticker),
                        List.of(new FieldError("Asset", "ticker", "Asset not found")))
        );
        return AssetInfo.from(asset);
    }

    public List<AssetInfo> getAllAssetInfos(){
        return assetRepository.findAll().stream().map(AssetInfo::from).toList();
    }
}
