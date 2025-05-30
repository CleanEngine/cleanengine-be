package com.cleanengine.coin.realitybot;

import com.cleanengine.coin.realitybot.api.BithumbAPIClientTest;
import com.cleanengine.coin.realitybot.api.RefresherRunnerTest;
import com.cleanengine.coin.realitybot.api.UnitPriceRefresherTest;
import com.cleanengine.coin.realitybot.dto.OpeningPriceTest;
import com.cleanengine.coin.realitybot.vo.UnitPricePolicyTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        RefresherRunnerTest.class,
        BithumbAPIClientTest.class,
        UnitPriceRefresherTest.class,
        OpeningPriceTest.class,
        UnitPricePolicyTest.class
})
public class RealitybotCoreTestSuite {
}
