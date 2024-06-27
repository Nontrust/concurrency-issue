package com.concurrency.issue.exception;

public class InvalidStockException extends RuntimeException{
    public InvalidStockException() {
        super();
    }
    public InvalidStockException(String msg) {
        super(msg);
    }
}
