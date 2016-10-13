package com.canyinghao.canokhttp;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.SparseArray;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;

/**
 * Created by jianyang on 2016/10/13.
 */

public final class CanCallManager {


    private static Map<Class<?>, SparseArray<Call>> allCallsMap = new ConcurrentHashMap<>();


    /**
     * 保存请求集合
     *
     * @param tag  请求标识
     * @param call 请求
     */
     static void putCall(Class<?> tag, Call call) {

        if (null != tag) {
            SparseArray<Call> callList = allCallsMap.get(tag);
            if (null == callList) {
                callList = new SparseArray<>();
            }
            callList.put(call.hashCode(), call);
            allCallsMap.put(tag, callList);

        }
    }

    /**
     * 取消请求
     *
     * @param tag 请求标识
     */
    public static void cancelCallByActivityDestroy(Class<?> tag) {
        if (null == tag)
            return;
        SparseArray<Call> callList = allCallsMap.get(tag);
        if (null != callList) {
            final int len = callList.size();
            for (int i = 0; i < len; i++) {
                Call call = callList.valueAt(i);
                if (null != call && !call.isCanceled())
                    call.cancel();
            }
            callList.clear();
            allCallsMap.remove(tag);

        }
    }

    /**
     * 取消请求
     *
     * @param tag  请求标识
     * @param call 请求
     */
     static void cancelCall(Class<?> tag, Call call) {

        if (null != call && null != tag) {
            SparseArray<Call> callList = allCallsMap.get(tag);
            if (null != callList) {
                Call c = callList.get(call.hashCode());
                if (null != c && !c.isCanceled())
                    c.cancel();
                callList.delete(call.hashCode());
                if (callList.size() == 0) {
                    allCallsMap.remove(tag);
                }


            }
        }
    }


}
