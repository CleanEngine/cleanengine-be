package com.cleanengine.coin.user.login.presentation;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Value("${spring.security.cookie.secure}")
    private boolean isCookieSecure;

    public record TokenCheckData(String message, Integer userId) {}

    @GetMapping("/api/logout")
    public ApiResponse<String> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("access_token", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(isCookieSecure);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ApiResponse.success("Logout Completed", HttpStatus.OK);
    }

    @GetMapping("/api/healthcheck")
    public ApiResponse<String> healthcheck() {
        return ApiResponse.success("Health Check Completed", HttpStatus.OK);
    }

    @GetMapping("/api/tokencheck")
    public ApiResponse<TokenCheckData> tokenCheck(@AuthenticationPrincipal CustomOAuth2User user) {

        return ApiResponse.success(new TokenCheckData("Token is Valid", user.getUserId()), HttpStatus.OK);

    }

}
