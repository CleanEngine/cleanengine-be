package com.cleanengine.coin.realitybot.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
public class DeviationPricePolicyTest {
    private final DeviationPricePolicy policy = new DeviationPricePolicy();

    @Test
    @DisplayName("1% 이하 편차라면 보정안한다.")
    void noAjustWhenDeviationLessThan1(){
        DeviationPricePolicy.AdjustPrice result = policy.adjust(15000,14000, 0,14500,100);

        assertEquals(15000,result.sell());
        assertEquals(14000,result.buy());
    }

    @Test
    @DisplayName("시장이 고평가일 때 보정한다.")
    void adjustWhenOverValue(){
        var result = policy.adjust(25000,24000, 0.05,19000,1000);
        System.out.println(result.sell());
        System.out.println(result.buy());
        assertTrue(result.sell() < 25000);
        assertTrue(result.buy() < 24000);
    }
    @Test
    @DisplayName("시장이 저평가일 때 보정한다.")
    void adjustWhenUnderValue(){
        var result = policy.adjust(15000,14000, -0.05,19000,1000);
        System.out.println(result.sell());
        System.out.println(result.buy());
        assertTrue(result.sell() > 15000);
        assertTrue(result.buy() > 14000);
    }
}