package com.cleanengine.coin.order.infra;

import com.cleanengine.coin.order.domain.SellOrder;
import org.springframework.data.repository.CrudRepository;

public interface SellOrderRepository extends CrudRepository<SellOrder, Long> {
}
