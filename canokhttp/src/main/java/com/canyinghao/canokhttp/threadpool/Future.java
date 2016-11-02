package com.canyinghao.canokhttp.threadpool;

/**
 * Created by yangjian on 16/6/23.
 */
public interface Future<T> {
    void cancel();

    boolean isCancelled();

    boolean isDone();

    T get();

    void waitDone();
}