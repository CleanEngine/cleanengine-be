package com.cleanengine.coin.user.login.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JWTUtilTest {

    @Autowired
    private JWTUtil jwtUtil;

    @DisplayName("JWT 토큰 생성")
    @Test
    public void justCreateJWTToken() {

        System.out.println("--------------------JWT Token ----------------------");
        for (int i = 1; i <= 10; i++)
            System.out.println("UserID " + i + " : " + jwtUtil.createJwt(i, "sample", "12345", 60 * 60 * 24 * 1000 * 100L));

    }

}