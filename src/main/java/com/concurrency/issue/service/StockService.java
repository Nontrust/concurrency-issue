package com.concurrency.issue.service;

import com.concurrency.issue.domain.Stock;
import com.concurrency.issue.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    @Transactional
    public void decreaseWithTransactionAnnotation(Long id, Long quantity){
        this.stockControl(id, quantity);
    }

    public synchronized void decreaseWithSynchronizedKeyword(Long id, Long quantity){
        this.stockControl(id, quantity);
    }

    private void stockControl(Long id, Long quantity){
        // Stock 조회
        Stock stock = stockRepository.findById(id).orElseThrow();
        // 재고감소
        stock.decrease(quantity);

        //갱신값 저장
        stockRepository.save(stock);
    }
}
