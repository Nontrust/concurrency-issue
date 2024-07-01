package com.concurrency.issue.service;

import com.concurrency.issue.domain.Stock;
import com.concurrency.issue.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    @Transactional
    public void decrease(Long id, Long quantity){
        // Stock 조회
        Stock stock = stockRepository.findById(id).orElseThrow();
        // 재고감소
        stock.decrease(quantity);
        //갱신값 저장
        stockRepository.save(stock);
    }

    @Transactional(propagation= REQUIRES_NEW)
    public void decreaseWhenRequireNewPropagation(Long id, Long quantity){
        Stock stock = stockRepository.findById(id).orElseThrow();
        // 재고감소
        stock.decrease(quantity);
        //갱신값 저장
        stockRepository.save(stock);
    }

    public synchronized void decreaseWithSynchronizedKeyword(Long id, Long quantity){
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);
        stockRepository.save(stock);
    }

    @Transactional
    public void decreaseWithPessimisticLock(Long id, Long quantity){
        Stock stock = stockRepository.findByIdWithPessimisticWrite(id).orElseThrow();
        stock.decrease(quantity);
        stockRepository.save(stock);
    }

    @Transactional
    public void decreaseWithWithOptimisticLock(Long id, Long quantity){
        Stock stock = stockRepository.findByIdWithOptimisticLock(id).orElseThrow();
        stock.decrease(quantity);
        stockRepository.save(stock);
    }
}
