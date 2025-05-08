package com.cleanengine.coin.user.info.infra;

import com.cleanengine.coin.user.domain.User;
import com.cleanengine.coin.user.login.infra.UserOAuthDetails;
import com.cleanengine.coin.user.info.presentation.UserInfoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("""
        SELECT new com.cleanengine.coin.user.login.infra.UserOAuthDetails(
                u.id,
                o.provider,
                o.providerUserId,
                o.email,
                o.nickname
            )
        FROM User u
        JOIN OAuth o ON u.id = o.userId
        WHERE o.provider = :provider
          AND o.providerUserId = :providerUserId
    """)
    UserOAuthDetails findUserByOAuthProviderAndProviderId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);


    @Query("""
        SELECT new com.cleanengine.coin.user.info.presentation.UserInfoDTO(
                u.id,
                o.email,
                o.nickname,
                o.provider,
                a.cash
            )
        FROM User u
        JOIN OAuth o ON u.id = o.userId
        LEFT JOIN Account a ON a.userId = u.id
        WHERE o.provider = :provider
          AND o.providerUserId = :providerUserId
    """)
    UserInfoDTO findUserInfoByProviderAndProviderId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);

}
