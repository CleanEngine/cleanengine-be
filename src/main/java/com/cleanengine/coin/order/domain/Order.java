package com.cleanengine.coin.order.domain;

import com.cleanengine.coin.common.error.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name="ticker", length = 10, nullable = false, updatable = false)
    protected String ticker;

    @Column(name="user_id", nullable = false, updatable = false)
    protected Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name="state", nullable = false)
    protected OrderStatus state;

    // TODO orderSize를 VO로 바꾸어야 함
    @Column(name="order_size")
    protected Double orderSize;

    // TODO price를 VO로 바꾸어야 함
    @Column(name="price", nullable = true)
    protected Double price;

    @Column(name="remaining_size", nullable = true)
    protected Double remainingSize;

    @Column(name="created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @Column(name="is_marketorder", nullable = false, updatable = false)
    protected Boolean isMarketOrder;

    @Column(name="is_bot", nullable = false, updatable = false)
    protected Boolean isBot;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClass(), this.id);
    }

    public void decreaseRemainingSize(Double amount) {
        if(amount == null){
            throw new IllegalArgumentException("감소시킬 잔량은 null일 수 없습니다.");
        }
        if (remainingSize >= amount) {
            remainingSize -= amount;
        } else {
            throw new IllegalArgumentException("주문의 잔여 수량은 0 이상이어야 합니다.");
        }
    }

    public void setState(OrderStatus state) {
        if(state == null) throw new IllegalArgumentException("OrderState cannot be null");
        this.state = state;
    }

    protected static void handleValidationErrors(List<FieldError> errors) {
        if(!errors.isEmpty()){
            throw new DomainValidationException(
                    "Validation Error occurred Creating Order", errors);
        }
    }
}
