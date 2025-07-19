package com.cleanengine.coin.common.util;

import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtil {

    public Optional<Integer> getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(CustomOAuth2User.class::isInstance)
                .map(CustomOAuth2User.class::cast)
                .map(CustomOAuth2User::getUserId);
    }

}
