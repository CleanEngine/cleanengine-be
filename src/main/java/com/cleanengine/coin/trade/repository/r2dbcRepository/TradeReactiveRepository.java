package com.cleanengine.coin.trade.repository.r2dbcRepository;

import com.cleanengine.coin.trade.entity.TradeR2DBC;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TradeReactiveRepository extends ReactiveCrudRepository<TradeR2DBC, Long> {

    Flux<TradeR2DBC> findByTicker(String ticker);

}
