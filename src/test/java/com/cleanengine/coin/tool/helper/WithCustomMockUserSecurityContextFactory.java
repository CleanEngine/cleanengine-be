package com.cleanengine.coin.tool.helper;

import com.cleanengine.coin.tool.annotation.WithCustomMockUser;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import com.cleanengine.coin.user.login.infra.UserOAuthDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomMockUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {
    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserOAuthDetails userOAuthDetails = new UserOAuthDetails();
        userOAuthDetails.setUserId(annotation.id());
        userOAuthDetails.setName(annotation.name());

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userOAuthDetails);

        Authentication authentication = new UsernamePasswordAuthenticationToken(customOAuth2User,
                null, customOAuth2User.getAuthorities());
        context.setAuthentication(authentication);

        return context;
    }
}
