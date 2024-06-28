package com.concurrency.issue.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OptimisticLockStockFacade {
    private final StockService stockService;

    /*** Optimistic lock facade ***/
    public void decreaseWithWithOptimisticLock(Long id, Long quantity) throws InterruptedException{
        while(true){
            try {
                stockService.decreaseWithWithOptimisticLock(id, quantity);
                break;
            } catch (Exception e){
                log.debug("retry Optimistic ::: {}, on this Thread ::: {}", e.getMessage(), Thread.currentThread().getName());
                Thread.sleep(50);
            }
        }
    }
}
