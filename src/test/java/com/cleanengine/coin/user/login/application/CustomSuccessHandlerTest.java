package com.cleanengine.coin.user.login.application;

import static org.mockito.Mockito.*;

import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@DisplayName("인증 성공 핸들러 단위테스트")
@ExtendWith(MockitoExtension.class)
class CustomSuccessHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private CustomOAuth2User customOAuth2User;

    private CustomSuccessHandler customSuccessHandler;

    @BeforeEach
    void setUp() {
        customSuccessHandler = new CustomSuccessHandler(jwtUtil, true, "frontend URL");
    }

    @DisplayName("인증 성공 시 JWT 토큰을 쿠키에 저장하고 FE로 리디렉션한다.")
    @Test
    void whenAuthenticationSuccess_thenSetCookieAndRedirect() throws Exception {
        // given
        int userId = 1;
        when(authentication.getPrincipal()).thenReturn(customOAuth2User);
        when(customOAuth2User.getUserId()).thenReturn(userId);
        when(jwtUtil.createJwt(eq(userId), anyLong())).thenReturn("test.jwt.token");

        // when
        customSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(response).addCookie(any(Cookie.class));
        verify(response).sendRedirect("frontend URL");
    }

}