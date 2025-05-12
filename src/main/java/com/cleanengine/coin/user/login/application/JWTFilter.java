package com.cleanengine.coin.user.login.application;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.common.response.ErrorResponse;
import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import com.cleanengine.coin.user.login.infra.UserOAuthDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/api/login") ||
                requestUri.startsWith("/api/oauth2") ||
                requestUri.startsWith("/api/healthcheck") ||
                requestUri.startsWith("/h2-console") ||
                requestUri.startsWith("/favicon.ico")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 쿠키에서 토큰 조회
        String authorizationToken = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("access_token")) {
                authorizationToken = cookie.getValue();
            }
        }

        // 토큰이 없으면 401 반환
        if (authorizationToken == null || jwtUtil.isExpired(authorizationToken)) {
            ErrorResponse err = ErrorResponse.of(ErrorStatus.UNAUTHORIZED_RESOURCE);
            ResponseEntity<ApiResponse<Object>> responseEntity = ApiResponse.fail(err).toResponseEntity();
            ObjectMapper objectMapper = new ObjectMapper();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(objectMapper.writeValueAsString(responseEntity.getBody()));
            return;
        }

        UserOAuthDetails userOAuthDetails = new UserOAuthDetails();
        userOAuthDetails.setUserId(jwtUtil.getUserId(authorizationToken));
        userOAuthDetails.setProvider(jwtUtil.getProvider(authorizationToken));
        userOAuthDetails.setProviderUserId(jwtUtil.getProviderUserId(authorizationToken));
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userOAuthDetails);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User,
                null, customOAuth2User.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

}
