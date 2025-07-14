package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.common.response.ErrorResponse;
import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.common.util.SecurityUtil;
import com.cleanengine.coin.order.application.OrderCancelService;
import com.cleanengine.coin.user.info.application.AccountService;
import com.cleanengine.coin.user.info.application.UserService;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    private final AccountService accountService;

    private final SecurityUtil securityUtil;

    private final OrderCancelService orderCancelService;

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

    @Operation(summary = "사용자 자산 초기화 (쿠키의 유저ID 사용)")
    @PostMapping("/api/account/reset")
    public ApiResponse<UserInfoDTO> resetAccount() {

        try {
            // 초기화 로직
            Optional<Integer> currentUserId = securityUtil.getCurrentUserId();
            if (currentUserId.isEmpty()) {
                log.debug("사용자 자산 초기화 실패: 계정 없음");
                return ApiResponse.fail(ErrorResponse.of(ErrorStatus.UNAUTHORIZED_RESOURCE));
            }
            int userId = currentUserId.get();
            orderCancelService.cancelAllForReset(userId);
            accountService.resetWithWallets(userId);

            return ApiResponse.success(null, HttpStatus.OK);
        } catch (Exception e) {
            log.error("사용자 자산 초기화 에러: {}", e.getMessage(), e);
            return ApiResponse.fail(ErrorResponse.of(ErrorStatus.INTERNAL_SERVER_ERROR));
        }
    }

}
