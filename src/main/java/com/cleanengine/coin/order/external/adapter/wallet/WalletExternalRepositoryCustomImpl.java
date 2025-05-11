package com.cleanengine.coin.order.external.adapter.wallet;

import com.cleanengine.coin.user.domain.Wallet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
@RequiredArgsConstructor
public class WalletExternalRepositoryCustomImpl implements WalletExternalRepositoryCustom {
    private final EntityManager em;

    @Override
    public Optional<Wallet> findWalletBy(Integer userId, String ticker) {
        TypedQuery<Wallet> query = em.createQuery(
                "select w from Wallet w inner join Account a on w.accountId = a.id where a.userId = :userId and w.ticker = :ticker",
                Wallet.class);
        query.setParameter("userId", userId);
        query.setParameter("ticker", ticker);

        try{
            Wallet wallet = query.getSingleResult();
            return Optional.of(wallet);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
