package com.cleanengine.coin.mypage.service;

import com.cleanengine.coin.mypage.dto.RankingDto;
import com.cleanengine.coin.mypage.infra.CurrentPriceCache;
import com.cleanengine.coin.mypage.repository.MyRankingRepository;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class RankingSchedulerService {

    private final MyRankingRepository myRankingRepository;
    private final AccountRepository accountRepository;
    private final Map<Integer,Double> roiCache = new ConcurrentHashMap<>();
    private final CurrentPriceCache currentPriceCache;
    private List<RankingDto> rankingDtoList = new ArrayList<>();



    @Scheduled(fixedRate = 1000)
    public void getROIRanking(){
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            Integer userId = account.getUserId();
            double cash = account.getCash();

            List<Wallet> wallets = myRankingRepository.findAllByAccountId(userId);
            double marketValue = 0;
            double purchaseValue = 0;
            for(Wallet wallet : wallets){
                String ticker = wallet.getTicker();
                double size = Optional.ofNullable(wallet.getSize()).orElse(0.0);
                double buyPrice = Optional.ofNullable(wallet.getBuyPrice()).orElse(0.0);
                double currentPrice = currentPriceCache.getCurrentPrice(ticker);

                marketValue += size*currentPrice;
                purchaseValue += size*buyPrice;
            }
            double totalValue = cash + purchaseValue;
            double roi = (purchaseValue == 0) ? 0 : ((totalValue - purchaseValue / purchaseValue) * 100);
            roiCache.put(userId, roi);
            System.out.println("userid : "+userId+" / roi : "+roi);
        }
        getRankingAll();
    }



    public void getRankingAll(){
        AtomicInteger rank = new AtomicInteger(1);
        rankingDtoList = roiCache.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .map(e -> new RankingDto(
                        rank.getAndIncrement(),
                        e.getKey(),
                        e.getValue()
                ))
                .collect(Collectors.toList());
    }

    public List<RankingDto> getMyRanking(Integer userId){
        int myIndex = IntStream.range(0, rankingDtoList.size())
                .filter(i -> rankingDtoList.get(i).getId().equals(userId))
                .findFirst()
                .orElse(-1);

        if (myIndex == -1) {
            return Collections.emptyList(); // 유저가 랭킹에 없음
        }

        int from = Math.max(0, myIndex - 2);
        int to = Math.min(rankingDtoList.size(), myIndex + 3); // +3은 본인 포함하여 아래 2명까지

        return rankingDtoList.subList(from, to);
    }
}
