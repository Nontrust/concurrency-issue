package com.concurrency.issue.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import static java.time.Duration.ofMillis;

@RequiredArgsConstructor
@Repository
public class RedisLockRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public Boolean lock(Long id){
        return redisTemplate
                .opsForValue()
                .setIfAbsent(generateKey(id), "lock", ofMillis(3_000));

    }

    public Boolean unlock(Long id){
        return redisTemplate.
                delete(generateKey(id));
    }

    private String generateKey(Long id){
        return id.toString();
    }

}
