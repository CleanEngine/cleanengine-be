package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.user.login.application.JWTUtil;
import com.cleanengine.coin.user.info.application.UserService;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;

    public UserController(UserService userService, JWTUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/api/userinfo")
    public ApiResponse<UserInfoDTO> retrieveUserInfo(HttpServletRequest request) {
        // TODO : user 정보 받아오는 공통 메서드 생성
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        }

        Cookie[] cookies = request.getCookies();
        String accessToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        String provider = jwtUtil.getProvider(accessToken);
        String providerUserId = jwtUtil.getProviderUserId(accessToken);
        UserInfoDTO userInfoDTO = userService.retrieveUserInfo(provider, providerUserId);

        return ApiResponse.success(userInfoDTO, HttpStatus.OK);
    }

}
