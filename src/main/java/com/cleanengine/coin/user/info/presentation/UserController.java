package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.application.AccountService;
import com.cleanengine.coin.user.info.application.WalletService;
import com.cleanengine.coin.user.login.application.JWTUtil;
import com.cleanengine.coin.user.info.application.UserService;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;
    private final AccountService accountService;
    private final WalletService walletService;
    private final JWTUtil jwtUtil;

    public UserController(UserService userService, AccountService accountService, WalletService walletService, JWTUtil jwtUtil) {
        this.userService = userService;
        this.accountService = accountService;
        this.walletService = walletService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/api/userinfo")
    public ApiResponse<UserInfoDTO> retrieveUserInfo(HttpServletRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            Integer userId = oAuth2User.getUserId();
            UserInfoDTO userInfoDTO = userService.retrieveUserInfoByUserId(userId);
            Account account = accountService.retrieveAccountByUserId(userId);
            List<Wallet> wallets = walletService.retrieveWalletsByAccountId(account.getId());
            userInfoDTO.setWallets(wallets);

            return ApiResponse.success(userInfoDTO, HttpStatus.OK);
        }

        throw new IllegalStateException("인증된 사용자를 찾을 수 없습니다.");
    }

}
