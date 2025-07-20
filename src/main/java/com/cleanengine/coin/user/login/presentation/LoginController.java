package com.cleanengine.coin.user.login.presentation;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "토큰 유효성 검사 응답 데이터")
    public record TokenCheckData(
            @Schema(description = "응답 메시지", example = "Token is Valid")
            String message,
            @Schema(description = "사용자 ID", example = "3")
            Integer userId) {

    }

    @Operation(summary = "사용자 로그아웃을 처리한다. (쿠키 토큰 제거)")
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

    @Operation(summary = "서비스가 살아있는지 확인한다.")
    @GetMapping("/api/healthcheck")
    public ApiResponse<String> healthcheck() {
        return ApiResponse.success("Health Check Completed", HttpStatus.OK);
    }

    @Operation(summary = "토큰이 유효한지 체크한다.")
    @GetMapping("/api/tokencheck")
    public ApiResponse<TokenCheckData> tokenCheck(@AuthenticationPrincipal CustomOAuth2User user) {
        return ApiResponse.success(new TokenCheckData("Token is Valid", user.getUserId()), HttpStatus.OK);
    }

}
