package com.concurrency.issue.repository;

import com.concurrency.issue.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class LettuceLockRepository {
    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while(!redisLockRepository.lock(id)) {
            Thread.sleep(100);
        }
        try {
            stockService.decrease(id, quantity);
        } catch (Exception e){
            log.debug("decrease exception ::: {} ", e.getMessage());
        } finally {
            redisLockRepository.unlock(id);
        }
    }
}
