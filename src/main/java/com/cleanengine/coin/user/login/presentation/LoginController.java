package com.cleanengine.coin.user.login.presentation;

import com.cleanengine.coin.common.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Value("${spring.security.cookie.secure}")
    private boolean isCookieSecure;

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

}
