package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.user.login.application.JWTUtil;
import com.cleanengine.coin.user.info.application.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
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
