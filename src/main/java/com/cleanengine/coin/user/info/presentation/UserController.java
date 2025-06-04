package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.common.response.ErrorResponse;
import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.application.AccountService;
import com.cleanengine.coin.user.info.application.WalletService;
import com.cleanengine.coin.user.info.application.UserService;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
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

    public UserController(UserService userService, AccountService accountService, WalletService walletService) {
        this.userService = userService;
        this.accountService = accountService;
        this.walletService = walletService;
    }

    @GetMapping("/api/userinfo")
    public ApiResponse<UserInfoDTO> retrieveUserInfo() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            Integer userId = oAuth2User.getUserId();
            UserInfoDTO userInfoDTO = userService.retrieveUserInfoByUserId(userId);
            if (userInfoDTO == null) {
                return ApiResponse.fail(ErrorResponse.of(ErrorStatus.UNAUTHORIZED_RESOURCE));
            }
            Account account = accountService.retrieveAccountByUserId(userId);
            List<Wallet> wallets = walletService.retrieveWalletsByAccountId(account.getId());
            userInfoDTO.setWallets(wallets);

            return ApiResponse.success(userInfoDTO, HttpStatus.OK);
        }
        return ApiResponse.fail(ErrorResponse.of(ErrorStatus.UNAUTHORIZED_RESOURCE));
    }

}
