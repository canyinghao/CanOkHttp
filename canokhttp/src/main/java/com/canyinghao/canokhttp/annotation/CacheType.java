package com.canyinghao.canokhttp.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 缓存类型
 *
 * @author canyinghao
 */
@IntDef({CacheType.NETWORK, CacheType.CACHE, CacheType.NETWORK_CACHE,
        CacheType.CACHE_NETWORK, CacheType.CACHETIME_NETWORK,
        CacheType.CACHETIME_NETWORK_CACHE})
@Retention(RetentionPolicy.SOURCE)
public @interface CacheType {

    int NETWORK = 1;
    int CACHE = 2;
    int NETWORK_CACHE = 3;
    int CACHE_NETWORK = 4;
    //  如果cachetime未过期，只读cache
    int CACHETIME_NETWORK = 5;
    //  如果cachetime未过期，只读cache，过期就请求，请求失败，读cache
    int CACHETIME_NETWORK_CACHE = 6;


}