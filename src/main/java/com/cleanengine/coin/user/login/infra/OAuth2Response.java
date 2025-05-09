package com.cleanengine.coin.user.login.infra;

public interface OAuth2Response {

    String getProvider();

    String getProviderId();

    String getEmail();

    String getName();

}
