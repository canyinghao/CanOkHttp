package com.canyinghao.canokhttp.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 缓存类型
 *
 * @author canyinghao
 */
@IntDef({CacheType.NETWORK, CacheType.CACHE, CacheType.NET_CACHE, CacheType.NETWORK_CACHE,
        CacheType.CACHE_NETWORK, CacheType.CACHETIME_NETWORK,
        CacheType.CACHETIME_NETWORK_CACHE})
@Retention(RetentionPolicy.SOURCE)
public @interface CacheType {
    //只请求网络
    int NETWORK = 0;
    //无cache请求网络，之后读cache
    int CACHE = 1;
    //请求成功存cache，失败调用失败
    int NET_CACHE = 2;
    //请求成功存cache，请求失败读cache，无cache失败
    int NETWORK_CACHE = 3;
    //先读cache，然后请求，请求成功存cache，失败调用失败
    int CACHE_NETWORK = 4;
    //  如果cachetime未过期，只读cache
    int CACHETIME_NETWORK = 5;
    //  如果cachetime未过期，只读cache，过期就请求，请求失败，读cache
    int CACHETIME_NETWORK_CACHE = 6;


}