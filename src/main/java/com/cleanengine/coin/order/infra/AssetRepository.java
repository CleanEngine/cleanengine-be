package com.cleanengine.coin.order.infra;

import com.cleanengine.coin.order.domain.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, String> {
}
