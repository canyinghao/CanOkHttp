package com.canyinghao.canokhttp.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 结果类型
 * @author canyinghao
 */
@IntDef({ResultType.SUCCESS, ResultType.FAIL_NO_NETWORK, ResultType.FAIL_PROTOCOL,
        ResultType.FAIL_NO_RESULT, ResultType.FAIL_URL_ERROR,
        ResultType.FAIL_NET_ERROR, ResultType.FAIL_CONNECTION_TIME_OUT,
        ResultType.FAIL_WRITE_READ_TIME_OUT, ResultType.FAIL_CONNECTION_INTERRUPTION,
        ResultType.FAIL_ON_UI_THREAD,ResultType.FAIL_SOME_WRONG})
@Retention(RetentionPolicy.SOURCE)
public @interface ResultType {
    int SUCCESS = 1;
    int FAIL_NO_NETWORK = 2;
    int FAIL_PROTOCOL = 3;
    int FAIL_NO_RESULT = 4;
    int FAIL_URL_ERROR = 5;
    int FAIL_NET_ERROR = 6;
    int FAIL_CONNECTION_TIME_OUT = 7;
    int FAIL_WRITE_READ_TIME_OUT = 8;
    int FAIL_CONNECTION_INTERRUPTION = 9;
    int FAIL_ON_UI_THREAD = 10;
    int FAIL_SOME_WRONG = 11;
}