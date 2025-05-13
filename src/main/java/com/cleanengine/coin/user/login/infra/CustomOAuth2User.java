package com.cleanengine.coin.user.login.infra;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final UserOAuthDetails userOAuthDetails;

    public CustomOAuth2User(UserOAuthDetails userOAuthDetails) {
        this.userOAuthDetails = userOAuthDetails;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return userOAuthDetails.getName();
    }

    public Integer getUserId() { return userOAuthDetails.getUserId(); }

    public String getProvider() {
        return userOAuthDetails.getProvider();
    }

    public String getProviderUserId() {
        return userOAuthDetails.getProviderUserId();
    }

}