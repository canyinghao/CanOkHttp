package com.canyinghao.canokhttp;

import android.app.Activity;
import android.app.Application;

import com.canyinghao.canokhttp.annotation.CacheType;

import java.io.File;
import java.util.List;
import java.util.Map;

import okhttp3.CookieJar;
import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * CanConfig
 *
 * @author canyinghao
 */

public final class CanConfig {

    private final int TIME_OUT = 30;
    private Application application;


    private boolean isJson;

    private int maxCacheSize;//缓存大小
    private File cachedDir;//缓存目录
    private int connectTimeout;//连接超时
    private int readTimeout;//读超时
    private int writeTimeout;//写超时
    private boolean retryOnConnectionFailure;//失败重新连接
    private int maxRetry;//最大重试次数
    private List<Interceptor> networkInterceptors;//网络拦截器
    private List<Interceptor> interceptors;//应用拦截器
    private int cacheSurvivalTime;//缓存存活时间（秒）
    private int cacheType;//缓存类型
    private int cacheNoHttpTime;//缓存时间，不请求网络的时间


    private String downloadFileDir;//下载文件保存目录

    private long downloadDelayTime;//下载完成后延迟事件

    private boolean isDownAccessFile;//是否断点下载
    private boolean isDownCoverFile;//是否覆盖下载

    private boolean isCacheInThread;//是否在线程中读缓存
    private boolean isOpenLog;//日志拦截器

    private boolean isHttpsTry;

//    0 都重试 1get 2 post
    private int httpsTryType;

//    0 no 1 get 2post  3 all
    private int publicType;

//    0 no 1 get 2 post 3 all
    private int useClientType;

    private boolean isUpLoadProgress;

    private boolean isApplicationJson;


    private String tag;
    private CookieJar cookieJar;

    private Map<String, String> globalParamMap ;  //全局参数
    private Map<String, String> globalGetParamMap ;  //全局参数
    private Map<String, String> globalHeaderMap ;  //全局请求头

    private OkHttpClient okHttpClient;

    private Dns dns;

    private String timeStamp;//时间戳
    //    其它请求方式，delete 、put 等
    private String otherMethod;

    public Application getApplication() {
        return application;
    }

    public CanConfig setApplication(Application application) {
        this.application = application;
        return this;
    }


    public CanConfig setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        return this;
    }

    public CanConfig setJson(boolean json) {
        isJson = json;
        return this;
    }

    public boolean isJson() {
        return isJson;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public CanConfig setCachedDir(File cachedDir) {
        if (null != cachedDir) {
            this.cachedDir = cachedDir;
        }

        return this;
    }

    public File getCachedDir() {
        return cachedDir;
    }

    public CanConfig setConnectTimeout(int connectTimeout) {
        if (connectTimeout <= 0) {
            connectTimeout = TIME_OUT;
        }
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public CanConfig setReadTimeout(int readTimeout) {
        if (readTimeout <= 0) {
            readTimeout = TIME_OUT;
        }
        this.readTimeout = readTimeout;
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public CanConfig setWriteTimeout(int writeTimeout) {
        if (writeTimeout <= 0) {
            writeTimeout = TIME_OUT;
        }
        this.writeTimeout = writeTimeout;
        return this;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public CanConfig setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
        this.retryOnConnectionFailure = retryOnConnectionFailure;
        return this;
    }

    public boolean isRetryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    public CanConfig setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;

        return this;
    }


    public int getMaxRetry() {
        return maxRetry;
    }

    public CanConfig setNetworkInterceptors(List<Interceptor> networkInterceptors) {
        if (null != networkInterceptors)
            this.networkInterceptors = networkInterceptors;
        return this;
    }

    public List<Interceptor> getNetworkInterceptors() {
        return networkInterceptors;
    }

    public CanConfig setInterceptors(List<Interceptor> interceptors) {
        if (null != interceptors)
            this.interceptors = interceptors;
        return this;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }


    public CanConfig setCacheSurvivalTime(int cacheSurvivalTime) {

        this.cacheSurvivalTime = cacheSurvivalTime;

        return this;
    }

    public int getCacheSurvivalTime() {

        return cacheSurvivalTime;
    }

    public CanConfig setCacheType(@CacheType int cacheType) {
        this.cacheType = cacheType;
        return this;
    }

    public int getCacheType() {
        return cacheType;
    }

    public CanConfig setCacheNoHttpTime(int cacheNoHttpTime) {
        this.cacheNoHttpTime = cacheNoHttpTime;

        return this;
    }

    public int getCacheNoHttpTime() {
        return cacheNoHttpTime;
    }


    public CanConfig setCacheInThread(boolean cacheInThread) {
        isCacheInThread = cacheInThread;
        return this;
    }

    public boolean isCacheInThread() {
        return isCacheInThread;
    }

    public CanConfig setTag(Object object) {
        if (object instanceof Activity) {
            Activity activity = (Activity) object;
            this.tag = activity.getClass().getCanonicalName();
        } else if (object instanceof android.support.v4.app.Fragment) {
            android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) object;
            this.tag = fragment.getActivity().getClass().getCanonicalName();
        } else if (object instanceof android.app.Fragment) {
            android.app.Fragment fragment = (android.app.Fragment) object;
            this.tag = fragment.getActivity().getClass().getCanonicalName();
        } else if (object != null) {
            this.tag = object.toString();
        }
        return this;
    }


    public String getTag() {
        return tag;
    }

    public CanConfig setDownloadFileDir(String downloadFileDir) {
        this.downloadFileDir = downloadFileDir;
        return this;
    }

    public String getDownloadFileDir() {
        return downloadFileDir;
    }


    public CanConfig setDownloadDelayTime(long downloadDelayTime) {
        this.downloadDelayTime = downloadDelayTime;
        return this;
    }

    public long getDownloadDelayTime() {
        return downloadDelayTime;
    }

    public CanConfig setDownAccessFile(boolean downAccessFile) {
        isDownAccessFile = downAccessFile;
        return this;
    }

    public boolean isDownAccessFile() {
        return isDownAccessFile;
    }

    public CanConfig setDownCoverFile(boolean downCoverFile) {
        isDownCoverFile = downCoverFile;
        return this;
    }
    public boolean isDownCoverFile() {
        return isDownCoverFile;
    }


    public CanConfig setCookieJar(CookieJar cookieJar) {
        if (cookieJar == null) throw new NullPointerException("cookieJar == null");
        this.cookieJar = cookieJar;
        return this;
    }

    public CookieJar getCookieJar() {
        return cookieJar;
    }

    public CanConfig setOpenLog(boolean openLog) {
        isOpenLog = openLog;
        return this;
    }

    public boolean isOpenLog() {
        return isOpenLog;
    }

    public boolean isUpLoadProgress() {
        return isUpLoadProgress;
    }

    public CanConfig setUpLoadProgress(boolean upLoadProgress) {
        isUpLoadProgress = upLoadProgress;
        return this;
    }

    public boolean isApplicationJson() {
        return isApplicationJson;
    }

    public CanConfig setApplicationJson(boolean applicationJson) {
        isApplicationJson = applicationJson;
        return this;
    }

    public boolean isHttpsTry() {
        return isHttpsTry;
    }

    public CanConfig setHttpsTry(boolean httpsTry) {
        isHttpsTry = httpsTry;

        return this;
    }

    public int getHttpsTryType() {
        return httpsTryType;
    }

    public CanConfig setHttpsTryType(int httpsTryType) {
        this.httpsTryType = httpsTryType;
        return this;
    }

    public CanConfig setGlobalParamMap(Map<String, String> globalParamMap) {
        this.globalParamMap = globalParamMap;
        return this;
    }




    public Map<String, String> getGlobalParamMap() {
        return globalParamMap;
    }

    public CanConfig setGlobalGetParamMap(Map<String, String> globalGetParamMap) {
        this.globalGetParamMap = globalGetParamMap;
        return this;
    }

    public Map<String, String> getGlobalGetParamMap() {
        return globalGetParamMap;
    }

    public CanConfig setGlobalHeaderMap(Map<String, String> globalHeaderMap) {
        this.globalHeaderMap = globalHeaderMap;

        return this;
    }




    public Map<String, String> getGlobalHeaderMap() {
        return globalHeaderMap;
    }


    public int getPublicType() {
        return publicType;
    }

    public CanConfig setPublicType(int publicType) {
        this.publicType = publicType;

        return this;
    }

    public CanConfig setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;

        return this;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }


    public CanConfig setDns(Dns dns) {
        this.dns = dns;

        return this;
    }

    public Dns getDns() {
        return this.dns ;
    }


    public CanConfig setUseClientType(int useClientType) {
        this.useClientType = useClientType;
        return this;
    }

    public int getUseClientType() {
        return useClientType;
    }


    public CanConfig setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public CanConfig clone() {

        return new CanConfig(this);

    }

    public String getOtherMethod() {
        return otherMethod;
    }

    public CanConfig setOtherMethod(String otherMethod) {
        this.otherMethod = otherMethod;
        return this;
    }

    public CanConfig() {
    }

    private CanConfig(CanConfig config) {


        this.application = config.application;
        this.isJson = config.isJson;
        this.maxCacheSize = config.maxCacheSize;
        this.cachedDir = config.cachedDir;
        this.connectTimeout = config.connectTimeout;
        this.readTimeout = config.readTimeout;
        this.writeTimeout = config.writeTimeout;
        this.retryOnConnectionFailure = config.retryOnConnectionFailure;
        this.maxRetry = config.maxRetry;
        this.networkInterceptors = config.networkInterceptors;
        this.interceptors = config.interceptors;
        this.cacheSurvivalTime = config.cacheSurvivalTime;
        this.cacheType = config.cacheType;
        this.cacheNoHttpTime = config.cacheNoHttpTime;
        this.downloadFileDir = config.downloadFileDir;
        this.downloadDelayTime = config.downloadDelayTime;
        this.isDownAccessFile = config.isDownAccessFile;
        this.isDownCoverFile = config.isDownCoverFile;
        this.isCacheInThread = config.isCacheInThread;

        this.tag = config.tag;
        this.cookieJar = config.cookieJar;
        this.globalParamMap = config.globalParamMap;
        this.globalGetParamMap = config.globalGetParamMap;
        this.globalHeaderMap = config.globalHeaderMap;
        this.isOpenLog = config.isOpenLog;
        this.isHttpsTry = config.isHttpsTry;
        this.httpsTryType = config.httpsTryType;
        this.publicType = config.publicType;
        this.useClientType = config.useClientType;
        this.isUpLoadProgress = config.isUpLoadProgress;
        this.timeStamp = config.timeStamp;
        this.isApplicationJson = config.isApplicationJson;



    }
}

