package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.common.response.ErrorResponse;
import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.user.info.application.UserService;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @Operation(summary = "쿠키의 유저ID를 통해 유저 정보와 보유 자산을 불러옵니다.")
    @GetMapping("/api/userinfo")
    public ApiResponse<UserInfoDTO> retrieveUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            Integer userId = oAuth2User.getUserId();
            UserInfoDTO userInfoDTO = userService.retrieveUserInfoByUserId(userId);

            if (userInfoDTO == null) {
                return ApiResponse.fail(ErrorResponse.of(ErrorStatus.UNAUTHORIZED_RESOURCE));
            }

            return ApiResponse.success(userInfoDTO, HttpStatus.OK);
        }
        return ApiResponse.fail(ErrorResponse.of(ErrorStatus.UNAUTHORIZED_RESOURCE));
    }

}
