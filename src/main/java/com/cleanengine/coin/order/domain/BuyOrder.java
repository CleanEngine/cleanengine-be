package com.cleanengine.coin.order.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// TODO AttributeOverride를 통해 Annotation 재지정 필요
@Entity(name = "buy_orders")
@Table(name="buy_orders")
@AttributeOverride(name="id", column=@Column(name="buy_order_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BuyOrder extends Order implements Comparable<BuyOrder> {
    @Column(name="locked_deposit", nullable = false, updatable = false)
    private Double lockedDeposit;

    @Column(name="remaining_deposit", nullable = false)
    private Double remainingDeposit;

    public static BuyOrder createMarketBuyOrder(Long id, String ticker, Integer userId, Double deposit,
                                                LocalDateTime createdAt, Boolean isBot) {
        // TODO command 객체의 validation으로 추출
        List<FieldError> errors = new ArrayList<>();
        if(deposit == null){
            errors.add(new FieldError("BuyOrder", "deposit", "deposit cannot be null"));
        }
        handleValidationErrors(errors);

        BuyOrder buyOrder = new BuyOrder(id, ticker, userId, OrderStatus.WAIT, null,
                null, null, createdAt, true, isBot, deposit, deposit);
        return buyOrder;
    }
  
    public static BuyOrder createLimitBuyOrder(Long id, String ticker, Integer userId, Double orderSize,
                                  Double price, LocalDateTime createdAt, Boolean isBot) {
        List<FieldError> errors = new ArrayList<>();
        if(orderSize == null){
            errors.add(new FieldError("BuyOrder", "orderSize", "orderSize cannot be null"));
        }
        if(price == null){
            errors.add(new FieldError("BuyOrder", "price", "price cannot be null"));
        }
        handleValidationErrors(errors);

        Double deposit = orderSize * price;
        BuyOrder buyOrder = new BuyOrder(id, ticker, userId, OrderStatus.WAIT, orderSize, price,
                orderSize, createdAt, false, isBot, deposit, deposit);
        return buyOrder;
    }

    @Override
    public int compareTo(BuyOrder order) {
        // 지정가 매수 가격 비교
        if(!this.isMarketOrder){
            // 매수 가격이 높다면 음수가 나와야 함
            int priceCompareResult = -Double.compare(this.price, order.price);
            if(priceCompareResult != 0) return priceCompareResult;
        }

        // 생성 시간 비교
        // 생성 시간이 빠르다면 음수가 나와야 함
        return this.createdAt.compareTo(order.createdAt);
    }

    public void decreaseRemainingDeposit(Double amount) {
        if(amount == null){
            throw new IllegalArgumentException("감소시킬 양은 null일 수 없습니다.");
        }

        if (remainingDeposit >= amount) {
            remainingDeposit -= amount;
        } else {
            throw new IllegalArgumentException("주문의 잔여 예수금은 0 이상이어야 합니다.");
        }
    }

    @Override
    public void decreaseRemainingSize(Double amount) {
        if(isMarketOrder){
            throw new IllegalArgumentException("시장가 매수 주문은 잔량을 수정할 수 없습니다.");
        }
        super.decreaseRemainingSize(amount);
    }

    protected BuyOrder(Long id, String ticker, Integer userId, OrderStatus state, Double orderSize,
                       Double price, Double remainingSize, LocalDateTime createdAt, Boolean isMarketOrder, Boolean isBot,
                       Double lockedDeposit, Double remainingDeposit) {
        super(id, ticker, userId, state, orderSize, price, remainingSize, createdAt, isMarketOrder, isBot);
        this.lockedDeposit = lockedDeposit;
        this.remainingDeposit = remainingDeposit;
    }
}
