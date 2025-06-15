package com.cleanengine.coin.realitybot;

import com.cleanengine.coin.realitybot.api.ApiSchedulerTest;
import com.cleanengine.coin.realitybot.api.BithumbAPIClientTest;
import com.cleanengine.coin.realitybot.config.ApiClientConfigTest;
import com.cleanengine.coin.realitybot.domain.APIVWAPStateTest;
import com.cleanengine.coin.realitybot.domain.PlatformVWAPStateTest;
import com.cleanengine.coin.realitybot.domain.VWAPCalculatorTest;
import com.cleanengine.coin.realitybot.dto.OpeningPriceTest;
import com.cleanengine.coin.realitybot.dto.TicksTest;
import com.cleanengine.coin.realitybot.parser.OpeningPriceParserTest;
import com.cleanengine.coin.realitybot.parser.TickParserTest;
import com.cleanengine.coin.realitybot.service.PlatformVWAPServiceTest;
import com.cleanengine.coin.realitybot.service.TickServiceManagerTest;
import com.cleanengine.coin.realitybot.vo.DeviationPricePolicyTest;
import com.cleanengine.coin.realitybot.vo.OrderPricePolicyTest;
import com.cleanengine.coin.realitybot.vo.UnitPricePolicyTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        BithumbAPIClientTest.class,
        OpeningPriceTest.class,
        PlatformVWAPStateTest.class,
        UnitPricePolicyTest.class,
        ApiSchedulerTest.class,
        ApiClientConfigTest.class,
        PlatformVWAPServiceTest.class,
        TickServiceManagerTest.class,
        VWAPCalculatorTest.class,
        APIVWAPStateTest.class,
        TicksTest.class,
        TickParserTest.class,
        OpeningPriceParserTest.class,
        OrderPricePolicyTest.class,
        DeviationPricePolicyTest.class
})
public class RealitybotCoreTestSuite {
}
