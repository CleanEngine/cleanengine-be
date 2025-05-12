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
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("access_token")) {
                    authorizationToken = cookie.getValue();
                }
            }
        }

        try {
            // 토큰이 없는 경우
            if (authorizationToken == null) {
                throw new IllegalArgumentException("Token not found");
            }

            // JWT 토큰 검증 및 클레임 파싱 (이 과정에서 서명 검증 실패하면 JwtException 발생)
            Integer userId = jwtUtil.getUserId(authorizationToken);
            String provider = jwtUtil.getProvider(authorizationToken);
            String providerUserId = jwtUtil.getProviderUserId(authorizationToken);

            // 토큰 만료 검증
            if (jwtUtil.isExpired(authorizationToken)) {
                throw new IllegalArgumentException("Token is expired");
            }

            Authentication authToken = getAuthentication(userId, provider, providerUserId);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            ErrorResponse err = ErrorResponse.of(ErrorStatus.UNAUTHORIZED_RESOURCE);
            ResponseEntity<ApiResponse<Object>> responseEntity = ApiResponse.fail(err).toResponseEntity();
            ObjectMapper objectMapper = new ObjectMapper();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(objectMapper.writeValueAsString(responseEntity.getBody()));
        }
    }

    private static Authentication getAuthentication(Integer userId, String provider, String providerUserId) {
        UserOAuthDetails userOAuthDetails = new UserOAuthDetails();
        userOAuthDetails.setUserId(userId);
        userOAuthDetails.setProvider(provider);
        userOAuthDetails.setProviderUserId(providerUserId);
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userOAuthDetails);

        // 스프링 시큐리티 인증 토큰 생성
        return new UsernamePasswordAuthenticationToken(customOAuth2User,
                null, customOAuth2User.getAuthorities());
    }

}
