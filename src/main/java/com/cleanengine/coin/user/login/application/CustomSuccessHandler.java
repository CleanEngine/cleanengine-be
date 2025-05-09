package com.cleanengine.coin.user.login.application;

import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Value("${spring.security.cookie.secure}")
    private boolean isCookieSecure;

    @Value("${frontend.url}")
    private String frontendUrl;

    public CustomSuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String provider = customUserDetails.getProvider();
        String providerUserId = customUserDetails.getProviderUserId();
        // TODO : JWT 토큰 만료시간 고려
        String token = jwtUtil.createJwt(provider, providerUserId, (long) 60 * 60 * 24 * 1000);

        response.addCookie(createCookie("access_token", token));
        response.sendRedirect(frontendUrl);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 24);
        cookie.setPath("/");

        cookie.setHttpOnly(true); // JS를 통해 쿠키 값을 읽을 수 없도록 설정
        cookie.setSecure(isCookieSecure);

        return cookie;
    }

}