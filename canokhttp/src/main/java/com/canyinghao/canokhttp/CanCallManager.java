package com.canyinghao.canokhttp;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;

/**
 * CanCallManager
 *
 * @author canyinghao
 */
public final class CanCallManager {


    private static Map<String, SparseArray<Call>> allCallsMap = new ConcurrentHashMap<>();


    /**
     * 保存请求集合
     *
     * @param tag  请求标识
     * @param call 请求
     */
    static void putCall(@NonNull String tag, @NonNull Call call) {


        try {

            SparseArray<Call> callList = null;
            if (allCallsMap.containsKey(tag)) {
                callList = allCallsMap.get(tag);

            }

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


        if (allCallsMap.containsKey(tag)) {

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

    }

    /**
     * 取消请求
     *
     * @param tag  请求标识
     * @param call 请求
     */
    static void cancelCall(@NonNull String tag, @NonNull Call call) {

        if (allCallsMap.containsKey(tag)) {

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


    /**
     * 是否存在tag
     *
     * @param tag 请求标识
     */
    static boolean isHaveTag(@NonNull String tag) {

        if (allCallsMap.containsKey(tag)) {

            return true;
        }
        return false;

    }

}
