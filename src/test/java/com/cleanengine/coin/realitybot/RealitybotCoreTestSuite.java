package com.cleanengine.coin.realitybot;

import com.cleanengine.coin.realitybot.api.BithumbAPIClientTest;
import com.cleanengine.coin.realitybot.api.RefresherRunnerTest;
import com.cleanengine.coin.realitybot.api.UnitPriceRefresherTest;
import com.cleanengine.coin.realitybot.dto.OpeningPriceTest;
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
})
public class RealitybotCoreTestSuite {
}
