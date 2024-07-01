package com.concurrency.issue.service;

import com.concurrency.issue.domain.Stock;
import com.concurrency.issue.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
class StockServiceTest {
    @Autowired
    private StockService stockService;
    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;

    @Autowired
    private NamedLockStockFacade namedLockStockFacade;

    @Autowired
    private LettuceLockService lettuceLockRepository;

    @Autowired
    private StockRepository stockRepository;

    int THREAD_COUNT = 100;
    int POOL_SIZE = 32;
    Long TARGET_PRODUCT_ID = 1L;

    @BeforeEach
    public void before(){
        stockRepository.save(Stock.of(TARGET_PRODUCT_ID, 100L));
    }

    @AfterEach
    public void after(){
        stockRepository.deleteAll();
    }

    @Test
    public void 재고감소(){
        stockService.decrease(TARGET_PRODUCT_ID, 1L);
        Stock stock = stockRepository.findById(TARGET_PRODUCT_ID).orElseThrow();

        assertEquals(stock.getQuantity(), 99);
    }

    @Test
    public void 동기화_함수에_동시에_100개_요청() throws Exception{
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);

        Runnable targetService = ()-> stockService.decreaseWithSynchronizedKeyword(TARGET_PRODUCT_ID,1L);

        //when
        스레드_서비스_생셩(executorService, countDownLatch, targetService);

        // then
        Stock decreasedStock = stockRepository.findById(TARGET_PRODUCT_ID).orElseThrow();
        assertEquals(0, decreasedStock.getQuantity());
    }


    @Test
    public void 트랜잭셔널_어노테이션에_동시에_100개_요청() throws Exception{
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);

        Runnable targetService = ()-> stockService.decrease(TARGET_PRODUCT_ID,1L);

        //when
        스레드_서비스_생셩(executorService, countDownLatch, targetService);

        // then
        Stock decreasedStock = stockRepository.findById(TARGET_PRODUCT_ID).orElseThrow();
        assertEquals(0, decreasedStock.getQuantity());

    }

    @Test
    public void 비관적_락사용_서비스에_동시에_100개_요청() throws Exception{
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);
        Runnable targetService = () -> stockService.decreaseWithPessimisticLock(TARGET_PRODUCT_ID, 1L);

        스레드_서비스_생셩(executorService, countDownLatch, targetService);
        countDownLatch.await();

        // then
        Stock decreasedStock = stockRepository.findById(TARGET_PRODUCT_ID).orElseThrow();
        assertEquals(0, decreasedStock.getQuantity());
    }

    @Test
    public void 낙관적_락사용_서비스_동시에_100개_요청() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);
        Runnable targetService = () -> {
            try {
                optimisticLockStockFacade.decreaseWithWithOptimisticLock(TARGET_PRODUCT_ID, 1L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        스레드_서비스_생셩(executorService, countDownLatch, targetService);
        countDownLatch.await();

        // then
        Stock decreasedStock = stockRepository.findById(TARGET_PRODUCT_ID).orElseThrow();
        assertEquals(0, decreasedStock.getQuantity());
    }

    @Test
    @Timeout(value = 3, unit = SECONDS)
    public void 네임드_락_서비스_동시에_100개_요청() throws Exception{
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);
        Runnable targetService = () -> {
            try {
                namedLockStockFacade.decrease(TARGET_PRODUCT_ID, 1L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        스레드_서비스_생셩(executorService, countDownLatch, targetService);
        countDownLatch.await();

        // then
        Stock decreasedStock = stockRepository.findById(TARGET_PRODUCT_ID).orElseThrow();
        assertEquals(0, decreasedStock.getQuantity());
    }

    @Test
//    @Timeout(value = 3, unit = SECONDS)
    public void 레디스_락_서비스_동시에_100개_요청() throws Exception{
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);
        Runnable targetService = () -> {
            try {
                lettuceLockRepository.decrease(TARGET_PRODUCT_ID, 1L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        스레드_서비스_생셩(executorService, countDownLatch, targetService);
        countDownLatch.await();

        // then
        Stock decreasedStock = stockRepository.findById(TARGET_PRODUCT_ID).orElseThrow();
        assertEquals(0, decreasedStock.getQuantity());
    }

    private void 스레드_서비스_생셩(ExecutorService executorService, CountDownLatch latch, Runnable runnable) throws Exception {
        IntStream.range(0, THREAD_COUNT)
                .forEach(value -> executorService.submit(()->
                {
                    try{
                        runnable.run();
                    } finally {
                        latch.countDown();
                    }
                }));
        latch.await();
        executorService.shutdown();
    }


}