package com.canyinghao.canokhttp.threadpool;



public interface JobContext {
    boolean isCanceled();

    String getTag();

}
