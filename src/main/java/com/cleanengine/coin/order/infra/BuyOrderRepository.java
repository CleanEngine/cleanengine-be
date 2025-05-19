package com.cleanengine.coin.order.infra;

import com.cleanengine.coin.order.domain.BuyOrder;
import org.springframework.data.repository.CrudRepository;

public interface BuyOrderRepository extends CrudRepository<BuyOrder, Long> {
}
