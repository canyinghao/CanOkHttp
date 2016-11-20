package com.canyinghao.canokhttp.threadpool;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WorkerManager {


    private static Map<String, SparseArray<Worker>> allCallsMap = new ConcurrentHashMap<>();


    /**
     * 保存请求集合
     *
     * @param tag  请求标识
     * @param call 请求
     */
    static void putCall(@NonNull String tag, @NonNull Worker call) {

        try {
            SparseArray<Worker> callList = allCallsMap.get(tag);
            if (null == callList) {
                callList = new SparseArray<>();
            }
            callList.put(call.hashCode(), call);
            allCallsMap.put(tag, callList);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 取消请求
     *
     * @param cls 请求标识
     */
    public static void cancelCallByActivityDestroy(@NonNull Class<?> cls) {


        String tag = cls.getCanonicalName();
        cancelCallByTag(tag);


    }


    /**
     * 取消请求
     *
     * @param tag 请求标识
     */
    public static void cancelCallByTag(@NonNull String tag) {


        SparseArray<Worker> callList = allCallsMap.get(tag);
        if (null != callList) {
            final int len = callList.size();
            for (int i = 0; i < len; i++) {
                Worker call = callList.valueAt(i);
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
    static void cancelCall(@NonNull String tag, @NonNull Worker call) {


        SparseArray<Worker> callList = allCallsMap.get(tag);
        if (null != callList) {
            Worker c = callList.get(call.hashCode());
            if (null != c && !c.isCanceled())
                c.cancel();
            callList.delete(call.hashCode());
            if (callList.size() == 0) {
                allCallsMap.remove(tag);
            }


        }

    }

    static void removeCall(@NonNull String tag, @NonNull Worker call) {

        SparseArray<Worker> callList = allCallsMap.get(tag);
        if (null != callList) {
            callList.delete(call.hashCode());
            if (callList.size() == 0) {
                allCallsMap.remove(tag);
            }


        }
    }


}
