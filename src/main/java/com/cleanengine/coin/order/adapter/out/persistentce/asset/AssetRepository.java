package com.cleanengine.coin.order.adapter.out.persistentce.asset;

import com.cleanengine.coin.order.domain.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface AssetRepository extends JpaRepository<Asset, String> {
    @Override
    List<Asset> findAll();
}
