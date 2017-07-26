package com.canyinghao.canokhttp.threadpool;


public interface Job<T> {
    T run();
}
