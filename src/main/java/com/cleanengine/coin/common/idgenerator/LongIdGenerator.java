package com.cleanengine.coin.common.idgenerator;

import java.time.LocalDateTime;

public interface LongIdGenerator {

    long nextId();

    LocalDateTime extractDateTime(long id);
}
