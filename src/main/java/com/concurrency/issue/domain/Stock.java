package com.concurrency.issue.domain;

import com.concurrency.issue.exception.InvalidStockException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Stock {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private Long productId;
    private Long quantity;

    public static Stock of(Long productId, Long quantity){
        return Stock.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
    }

    public void decrease (Long quantity){
        if(this.quantity  - quantity < 0){
            throw new InvalidStockException("재고는 0개 미만이 될 수 없습니다.");
        }

        this.quantity -= quantity;
    }
}
