package com.cleanengine.coin.realitybot;

import com.cleanengine.coin.realitybot.api.ApiSchedulerTest;
import com.cleanengine.coin.realitybot.api.BithumbAPIClientTest;
import com.cleanengine.coin.realitybot.api.RefresherRunnerTest;
import com.cleanengine.coin.realitybot.api.UnitPriceRefresherTest;
import com.cleanengine.coin.realitybot.config.ApiClientConfigTest;
import com.cleanengine.coin.realitybot.config.SchedulerConfigTest;
import com.cleanengine.coin.realitybot.domain.APIVWAPStateTest;
import com.cleanengine.coin.realitybot.domain.PlatformVWAPStateTest;
import com.cleanengine.coin.realitybot.domain.VWAPCalculatorTest;
import com.cleanengine.coin.realitybot.dto.OpeningPriceTest;
import com.cleanengine.coin.realitybot.dto.TicksTest;
import com.cleanengine.coin.realitybot.parser.OpeningPriceParserTest;
import com.cleanengine.coin.realitybot.parser.TickParserTest;
import com.cleanengine.coin.realitybot.service.PlatformVWAPServiceTest;
import com.cleanengine.coin.realitybot.service.TickServiceManagerTest;
import com.cleanengine.coin.realitybot.service.VWAPerrorInJectionSchedulerTest;
import com.cleanengine.coin.realitybot.vo.UnitPricePolicyTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        RefresherRunnerTest.class,
        BithumbAPIClientTest.class,
        UnitPriceRefresherTest.class,
        OpeningPriceTest.class,
        PlatformVWAPStateTest.class,
        UnitPricePolicyTest.class,
        SchedulerConfigTest.class,
        ApiSchedulerTest.class,
        ApiClientConfigTest.class,
        PlatformVWAPServiceTest.class,
        TickServiceManagerTest.class,
        VWAPCalculatorTest.class,
        APIVWAPStateTest.class,
        TicksTest.class,
        TickParserTest.class,
        OpeningPriceParserTest.class,
        VWAPerrorInJectionSchedulerTest.class,
})
public class RealitybotCoreTestSuite {
}
