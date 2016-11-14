package com.canyinghao.canokhttp.threadpool;




public interface FutureListener<T> {
    void onFutureDone(Future<T> future);
}
