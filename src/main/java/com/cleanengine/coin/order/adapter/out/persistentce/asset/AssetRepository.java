package com.cleanengine.coin.order.adapter.out.persistentce.asset;

import com.cleanengine.coin.order.domain.Asset;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, String> {
    @WithSpan("api.request.call.all.asset")
    @Override
    List<Asset> findAll();
}
