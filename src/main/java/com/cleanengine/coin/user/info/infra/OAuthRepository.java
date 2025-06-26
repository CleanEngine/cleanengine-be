package com.cleanengine.coin.user.info.infra;

import com.cleanengine.coin.user.domain.OAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface OAuthRepository extends JpaRepository<OAuth, Long> {

    OAuth findByProviderAndProviderUserId(String provider, String providerUserId);

}
