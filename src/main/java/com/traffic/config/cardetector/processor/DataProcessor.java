package com.traffic.config.cardetector.processor;

public interface DataProcessor<T> {
    void process(T data);
}
