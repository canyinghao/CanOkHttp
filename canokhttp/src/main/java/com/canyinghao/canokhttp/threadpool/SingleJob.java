package com.canyinghao.canokhttp.threadpool;


public interface SingleJob<O,T> {

    T run(O o);
}
