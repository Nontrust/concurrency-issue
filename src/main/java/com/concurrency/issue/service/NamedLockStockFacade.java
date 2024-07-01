package com.concurrency.issue.service;

import com.concurrency.issue.repository.LockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NamedLockStockFacade {
    private final LockRepository lockRepository;
    private final StockService stockService;

    public void decrease(Long id, Long quantity){
        try{
            lockRepository.getLock(id.toString());
            stockService.decreaseWhenRequireNewPropagation(id, quantity);
        } catch (Exception e){
            log.debug("retry get Named Lock ::: {}", e.getMessage());
        } finally {
            lockRepository.releaseLock(id.toString());
        }

    }
}
