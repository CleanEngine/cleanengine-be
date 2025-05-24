package com.cleanengine.coin.order.infra.query;

import com.cleanengine.coin.order.domain.BuyOrder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyOrderQueryRepository extends CrudRepository<BuyOrder, Long> {
    @Query("select o from buy_orders o where o.state = 'WAIT'")
    List<BuyOrder> findIncompletedBuyOrders();
}
