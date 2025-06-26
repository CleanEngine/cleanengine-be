package com.cleanengine.coin.order.adapter.out.persistentce.order.command;

import com.cleanengine.coin.order.domain.BuyOrder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BuyOrderRepository extends CrudRepository<BuyOrder, Long> {
}
