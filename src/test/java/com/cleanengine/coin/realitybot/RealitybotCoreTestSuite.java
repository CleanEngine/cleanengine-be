package com.cleanengine.coin.realitybot;

import com.cleanengine.coin.realitybot.api.*;
import com.cleanengine.coin.realitybot.config.ApiClientConfigTest;
import com.cleanengine.coin.realitybot.config.SchedulerConfigTest;
import com.cleanengine.coin.realitybot.dto.OpeningPriceTest;
import com.cleanengine.coin.realitybot.service.PlatformVWAPServiceTest;
import com.cleanengine.coin.realitybot.service.TickServiceManager;
import com.cleanengine.coin.realitybot.service.TickServiceManagerTest;
import com.cleanengine.coin.realitybot.vo.UnitPricePolicyTest;
import com.cleanengine.coin.realitybot.vo.VWAPStateTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        RefresherRunnerTest.class,
        BithumbAPIClientTest.class,
        UnitPriceRefresherTest.class,
        OpeningPriceTest.class,
        VWAPStateTest.class,
        UnitPricePolicyTest.class,
        SchedulerConfigTest.class,
        ApiSchedulerTest.class,
        ApiClientConfigTest.class,
        PlatformVWAPServiceTest.class,
        TickServiceManagerTest.class
})
public class RealitybotCoreTestSuite {
}
