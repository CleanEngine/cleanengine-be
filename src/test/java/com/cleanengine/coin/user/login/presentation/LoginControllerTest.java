package com.cleanengine.coin.user.login.presentation;

import com.cleanengine.coin.user.login.application.JWTUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("로그인 및 토큰 API 통합테스트")
@AutoConfigureMockMvc
@SpringBootTest
class LoginControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JWTUtil jwtUtil;

    @Test
    @DisplayName("헬스체크 성공 시 성공 응답 반환")
    void healthcheckTest() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/healthcheck")
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").value("Health Check Completed"));
    }

    @Test
    @DisplayName("유효한 JWT 토큰과 함께 요청 시 토큰 검증에 성공한다.")
    void validTokenCheckTest() throws Exception {
        // given
        Integer userId = 123;
        Long expiredMs = 1000L * 60 * 60;
        String token = jwtUtil.createJwt(userId, expiredMs);
        Cookie cookie = new Cookie("access_token", token);
        cookie.setPath("/");

        // when
        ResultActions resultActions = mvc.perform(get("/api/tokencheck")
                .cookie(cookie)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.message").value("Token is Valid"))
                .andExpect(jsonPath("$.data.userId").value(userId));
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰으로 요청 시 토큰 검증에 실패한다.")
    void invalidTokenCheckTest() throws Exception {
        // given
        String invalidToken = "invalid.jwt.token";
        Cookie cookie = new Cookie("access_token", invalidToken);
        cookie.setPath("/");

        // when
        ResultActions resultActions = mvc.perform(get("/api/tokencheck")
                .cookie(cookie)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("로그아웃 시 access_token 쿠키가 제거된다")
    void logoutTest() throws Exception {
        // given
        Integer userId = 123;
        Long expiredMs = 1000L * 60 * 60;
        String token = jwtUtil.createJwt(userId, expiredMs);
        Cookie cookie = new Cookie("access_token", token);
        cookie.setPath("/");

        // when
        ResultActions resultActions = mvc.perform(get("/api/logout")
                .cookie(cookie)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().value("access_token", org.hamcrest.Matchers.emptyOrNullString()));
    }
}