package com.canyinghao.canokhttp.threadpool;


public interface Future<T> {
    void cancel();

    boolean isCanceled();

    boolean isDone();

    T get();

    void waitDone();
}