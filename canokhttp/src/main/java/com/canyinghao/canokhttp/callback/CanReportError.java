package com.canyinghao.canokhttp.callback;

public interface CanReportError {
    void report(String url,Throwable throwable,int code,String msg);
}
