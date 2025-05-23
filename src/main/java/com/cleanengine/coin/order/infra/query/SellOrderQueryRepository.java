package com.cleanengine.coin.order.infra.query;

import com.cleanengine.coin.order.domain.SellOrder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellOrderQueryRepository extends CrudRepository<SellOrder, Long> {
    @Query("select o from sell_orders o where o.state = 'WAIT'")
    List<SellOrder> findIncompletedSellOrders();
}
