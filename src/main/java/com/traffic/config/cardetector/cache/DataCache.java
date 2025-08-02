package com.traffic.config.cardetector.cache;

public interface DataCache<T> {
    void put(String key, T data);
    T get(String key);
    void remove(String key);
    void clear();
    int size();
}
