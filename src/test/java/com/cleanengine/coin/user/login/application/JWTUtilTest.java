package com.cleanengine.coin.user.login.application;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class JWTUtilTest {

    private final String secretKey = "secret-key-secret-key-secret-key-secret-key-secret-key-secret-key";
    private final JWTUtil jwtUtil = new JWTUtil(secretKey);

    @DisplayName("유저 ID와 유효기한으로 JWT를 생성한다.")
    @Test
    void createJwt() {
        // given
        int userId = 3;
        Long expiredMs = 1000L;

        // when
        String jwt = jwtUtil.createJwt(userId, expiredMs);

        // then
        assertThat(jwt).isNotNull();
        assertThat(jwtUtil.getUserId(jwt)).isEqualTo(userId);
        assertFalse(jwtUtil.isExpired(jwt));
    }

    @DisplayName("만료된 JWT를 감지한다.")
    @Test
    void expiredJwt() throws InterruptedException {
        // given
        int userId = 3;
        Long expiredMs = 1L;

        // when
        String jwt = jwtUtil.createJwt(userId, expiredMs);

        // then
        Thread.sleep(2L);
        assertThat(jwt).isNotNull();
        assertThatThrownBy(() -> jwtUtil.isExpired(jwt))
                .isInstanceOf(ExpiredJwtException.class);
    }

    // 위조 검증 (userId, 만료기한, secret key 각각)
}