package com.concurrency.issue.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedissonLockStockFacade {
    private final RedissonClient redissonClient;
    private final StockService stockService;

    public void decrease(Long id, Long quantity){
        RLock lock = redissonClient.getLock(generateKey(id));
        try{
            boolean lockResult = lock.tryLock(10, 1, SECONDS);

            if(!lockResult){
                log.warn("Lock acquisition failed ::: {}", id);
                return ;
            }
            stockService.decreaseWhenRequireNewPropagation(id, quantity);
        } catch (InterruptedException e){
            log.warn("Unable to acquire subscription lock::: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private String generateKey(Long id){
        return id.toString();
    }
}
