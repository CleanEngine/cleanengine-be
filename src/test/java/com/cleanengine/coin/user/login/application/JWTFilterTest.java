package com.cleanengine.coin.user.login.application;

import static org.mockito.Mockito.*;

import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.configuration.SecurityEndpoints.EndpointConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

@DisplayName("JWTFilter 단위테스트")
@ExtendWith(MockitoExtension.class)
class JWTFilterTest {

    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private EndpointConfig endpointConfig;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTFilter jwtFilter;

    private final String publicPath = "/api/public";
    private final String privatePath = "/api/private";
    private final int unauthorizedStatus = ErrorStatus.UNAUTHORIZED_RESOURCE.getHttpStatus().value();

    @DisplayName("public path 접근 시 인증없이 통과한다.")
    @Test
    void whenPublicPath_thenProceedWithoutValidation() throws Exception {
        // given
        when(request.getRequestURI()).thenReturn(publicPath);
        when(endpointConfig.isPublicPath(publicPath)).thenReturn(true);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).getUserId(any());
    }

    @DisplayName("private path 접근 시 토큰이 없으면 401 응답을 반환한다.")
    @Test
    void whenNoToken_thenReturnUnauthorized() throws Exception {
        // given
        when(request.getRequestURI()).thenReturn("/api/private");
        when(endpointConfig.isPublicPath("/api/private")).thenReturn(false);
        when(request.getCookies()).thenReturn(null);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(unauthorizedStatus);
        verify(filterChain, never()).doFilter(request, response);
    }

    @DisplayName("private path 접근 시 유효한 토큰인 경우 다음 필터체인으로 넘어간다.")
    @Test
    void whenValidToken_thenAuthenticate() throws Exception {
        // given
        when(request.getRequestURI()).thenReturn(privatePath);
        when(endpointConfig.isPublicPath(privatePath)).thenReturn(false);
        Cookie cookie = new Cookie("access_token", "valid_token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtUtil.getUserId("valid_token")).thenReturn(1);
        when(jwtUtil.isExpired("valid_token")).thenReturn(false);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }

    @DisplayName("private path 접근 시 만료된 토큰인 경우 401 응답을 반환한다.")
    @Test
    void whenExpiredToken_thenReturnUnauthorized() throws Exception {
        // given
        when(request.getRequestURI()).thenReturn(privatePath);
        when(endpointConfig.isPublicPath(privatePath)).thenReturn(false);
        Cookie cookie = new Cookie("access_token", "expired_token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtUtil.getUserId("expired_token")).thenReturn(1);
        when(jwtUtil.isExpired("expired_token")).thenReturn(true);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(unauthorizedStatus);
        verify(filterChain, never()).doFilter(request, response);
    }
}