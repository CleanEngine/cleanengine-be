package com.cleanengine.coin.realitybot.vo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnitPricePolicyTest {

    private UnitPricePolicy unitPricePolicy;

    @BeforeEach
    void setUp(){
        unitPricePolicy = new UnitPricePolicy();
        unitPricePolicy.initRules();
    }


    @DisplayName("opening_price가 단위 가격이 정확이 매핑되는 지 테스트")
    @Test
    void testGetUnitPrice(){

        //then
        //0원이 입력 될 경우
        assertEquals(0.00000001,unitPricePolicy.getUnitPrice(0));

        //최저가 보다 낮은 금액을 입력했을 때
        assertEquals(0.00000001,unitPricePolicy.getUnitPrice(0.0000999));
        assertEquals(0.0000001,unitPricePolicy.getUnitPrice(0.000999));

        //최고가 보다 높은 금액을 입력했을 때
        assertEquals(500,unitPricePolicy.getUnitPrice(1_999_999.9999999));
        assertEquals(1_000,unitPricePolicy.getUnitPrice(2_000_000.0000009));
    }
    @DisplayName("opening_price는 음수보다 높아야 합니다.")
    @Test
    void testNegativeValueThrowsException(){
        assertEquals(0.00000001,unitPricePolicy.getUnitPrice(-15));
    }
}