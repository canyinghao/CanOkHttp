package com.canyinghao.canokhttp.threadpool;


import androidx.annotation.Nullable;

public interface FutureListener<T> {
    void onFutureDone(@Nullable T future);
}
