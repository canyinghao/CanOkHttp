package com.canyinghao.canokhttp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.canyinghao.canokhttp.annotation.CacheType;
import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.annotation.ResultType;
import com.canyinghao.canokhttp.cache.ACache;
import com.canyinghao.canokhttp.callback.CanCallBack;
import com.canyinghao.canokhttp.cookie.PersistentCookieJar;
import com.canyinghao.canokhttp.cookie.cache.SetCookieCache;
import com.canyinghao.canokhttp.cookie.persistence.SharedPrefsCookiePersistor;
import com.canyinghao.canokhttp.handler.OkHandler;
import com.canyinghao.canokhttp.handler.OkMessage;
import com.canyinghao.canokhttp.model.FileLoadBean;
import com.canyinghao.canokhttp.progress.ProgressRequestBody;
import com.canyinghao.canokhttp.progress.ProgressResponseBody;
import com.canyinghao.canokhttp.threadpool.FutureListener;
import com.canyinghao.canokhttp.threadpool.Job;
import com.canyinghao.canokhttp.threadpool.ThreadPool;
import com.canyinghao.canokhttp.util.CanOkHttpUtil;
import com.socks.library.KLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.HttpRetryException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CookieJar;
import okhttp3.Dns;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * CanOkHttp
 *
 * @author canyinghao
 */

public final class CanOkHttp {


    //    全局的config，在Application里执行init得到
    private static CanConfig globalConfig;
    private static Map<String, ArrayList<String>> linesMap;
    private static long userId = 0;
    //  当前CanOkHttp持有的config，默认从globalConfig克隆得到
    public CanConfig mCurrentConfig;
    //    应用Application
    public Application mApplication;
    //    当前持有的OkHttpClient
    public OkHttpClient mCurrentHttpClient;
    //   传入所有参数后得到的请求体
    public Request mRequest;
    //  请求参数
    public Map<String, String> paramMap = new LinkedHashMap<>();
    //    请求头参数
    public Map<String, String> headerMap = new LinkedHashMap<>();
    //  可重复请求参数
    public Map<String, String> repeatMap = new IdentityHashMap<>();
    //  json请求参数
    public JSONObject jsonMap = new JSONObject();
    //  请求地址
    public String url = "";
    public String host = "";
    //  缓存的key，由url和参数得到
    public String cache_key = "";
    //  回调ui线程中
    public CanCallBack mCanCallBack;
    //  是否是下载或上传
    public boolean isDownOrUpLoad;
    //  已下载的文件大小
    public long completedSize;

    //下载状态
    public int downloadStatus = DownloadStatus.INIT;

    //是否已经初始化OkClient
    public boolean isInitOkClient;
    //是否是post方法
    public boolean isPost;

    public boolean isChangeLine;

    public int tempPublicType;

    public static CanOkHttp getInstance() {

        return new CanOkHttp();

    }

    public static void setLinesMap(Map<String, ArrayList<String>> linesMap) {
        CanOkHttp.linesMap = linesMap;
    }

    public static void setUserId(long userId) {
        CanOkHttp.userId = userId;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public String getMethod() {

        return isPost ? "POST" : "GET";

    }

    /**
     * 网络是否可用
     *
     * @param context 上下文
     * @return 是否
     */
    private boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("MissingPermission") final NetworkInfo net = cm.getActiveNetworkInfo();

            return net != null && net.getState() == NetworkInfo.State.CONNECTED;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 打印日志 日志开关在Application设置KLog.init(true,"Canyinghao");
     *
     * @param msg 日志信息
     */
    private void okHttpLog(String msg, boolean isResult) {
        if(mCurrentConfig.isOpenLog()){
            if (!isDownOrUpLoad && isResult && mCurrentConfig.isJson()) {
                KLog.json(msg);
            } else {
                KLog.d(msg);
            }
        }

    }




    private final Interceptor RETRY_INTERCEPTOR = new Interceptor() {


        @Override
        public Response intercept(Chain chain) throws IOException {

            Request request = chain.request();

            int retryNum = 0;
            Response response = chain.proceed(request);
            int code = response.code();
            while (!response.isSuccessful() && code != 500 && code != 404 && retryNum < mCurrentConfig.getMaxRetry()) {
                retryNum++;
                response = chain.proceed(request);
                code = response.code();
            }
            return response;
        }
    };

    /**
     * 日志拦截器
     */
    private final Interceptor LOG_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response res = null;
            okHttpLog(String.format("%s-URL: %s %n", chain.request().method(),
                    chain.request().url()), false);
            long startTime = System.currentTimeMillis();
            res = chain.proceed(chain.request());
            long endTime = System.currentTimeMillis();

            okHttpLog(String.format("CostTime: %.1fs", (endTime - startTime) / 1000.0), false);

            return res;
        }
    };


    /**
     * 进度拦截器
     */
    private final Interceptor PROGRESS_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());

            return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse, CanOkHttp.this))
                    .build();
        }
    };

    /**
     * 将全局Config克隆给当前Config
     */
    private CanOkHttp() {

        if (mCurrentConfig == null && globalConfig != null) {
            mCurrentConfig = globalConfig.clone();
        }

        if (mCurrentConfig != null) {
            mApplication = mCurrentConfig.getApplication();
        }

        if (mCurrentConfig == null || mApplication == null) {

            throw new IllegalArgumentException("please run init");
        }

    }


    /**
     * 通过当前的Config获取mCurrentHttpClient
     */
    private void initClient() {

        if (isDownOrUpLoad) {
            mCurrentHttpClient = getHttpClient(true);
        } else {
            mCurrentHttpClient = getHttpClient();
        }


    }

    /**
     * 取得一个 OkHttpClient
     *
     * @return OkHttpClient
     */
    public OkHttpClient getHttpClient() {
        return getHttpClient(false);
    }

    /**
     * 取得一个 OkHttpClient
     *
     * @return OkHttpClient
     */
    public OkHttpClient getHttpClient(boolean isNew) {

        if (!isNew) {
            if (mCurrentHttpClient != null) {
                return mCurrentHttpClient;
            }
            int useClientType = mCurrentConfig.getUseClientType();
            if ((useClientType == 1 && !isPost) ||
                    (useClientType == 2 && isPost) ||
                    useClientType == 3) {
                mCurrentHttpClient = globalConfig.getOkHttpClient();

                if (mCurrentHttpClient != null) {
                    return mCurrentHttpClient;
                }
            }

        }

        KLog.e("getHttpClient");

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(mCurrentConfig.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(mCurrentConfig.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(mCurrentConfig.getWriteTimeout(), TimeUnit.SECONDS);

        if (isDownOrUpLoad) {
            clientBuilder.addNetworkInterceptor(PROGRESS_INTERCEPTOR);
        }
        if (null != mCurrentConfig.getNetworkInterceptors() && !mCurrentConfig.getNetworkInterceptors().isEmpty())
            clientBuilder.networkInterceptors().addAll(mCurrentConfig.getNetworkInterceptors());
        if (null != mCurrentConfig.getInterceptors() && !mCurrentConfig.getInterceptors().isEmpty())
            clientBuilder.interceptors().addAll(mCurrentConfig.getInterceptors());
        if (mCurrentConfig.isOpenLog()&& BuildConfig.DEBUG)
            clientBuilder.addInterceptor(LOG_INTERCEPTOR);

        if (mCurrentConfig.isRetryOnConnectionFailure()) {
            clientBuilder.retryOnConnectionFailure(true);

            if (mCurrentConfig.getMaxRetry() > 0) {
                clientBuilder.addInterceptor(RETRY_INTERCEPTOR);
            }

        } else {
            clientBuilder.retryOnConnectionFailure(false);
        }

//        setSslSocketFactory(clientBuilder);

        if (null != mCurrentConfig.getCookieJar()) {
            clientBuilder.cookieJar(mCurrentConfig.getCookieJar());
        }

        if (null != mCurrentConfig.getDns()) {
            clientBuilder.dns(mCurrentConfig.getDns());
        }
        if(!mCurrentConfig.isProxy()){
            clientBuilder.proxy(Proxy.NO_PROXY);
        }

        OkHttpClient okHttpClient = clientBuilder.build();

        if (!isNew) {
            globalConfig.setOkHttpClient(okHttpClient);
        }

        return okHttpClient;


    }


    /**
     * 初始化OkClient
     *
     * @return CanOkHttp
     */
    public CanOkHttp initOkClient() {
        isInitOkClient = true;
        initClient();
        return this;
    }

    /**
     * 设置一个 OkHttpClient
     *
     * @param client OkHttpClient
     * @return CanOkHttp
     */
    public CanOkHttp setOkHttp(OkHttpClient client) {

        isInitOkClient = true;

        mCurrentHttpClient = client;

        return this;


    }


    /**
     * 设置当前Config
     *
     * @param currentConfig 当前Config
     * @return CanOkHttp
     */
    public CanOkHttp setCurrentConfig(CanConfig currentConfig) {
        this.mCurrentConfig = currentConfig;

        if (currentConfig != null) {
            mApplication = currentConfig.getApplication();
        }
        if (currentConfig == null || mApplication == null) {

            throw new IllegalArgumentException("please run init");
        }

        return this;
    }


    /**
     * @param str String
     * @return String
     */
    public String checkString(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return str;
    }

    /**
     * 添加参数
     *
     * @param key   键
     * @param value 值
     * @return CanOkHttp
     */
    public CanOkHttp add(@NonNull String key, @NonNull String value) {


        paramMap.put(checkString(key), checkString(value));

        return this;
    }


    /**
     * 添加可重复参数
     *
     * @param key   键
     * @param value 值
     * @return CanOkHttp
     */
    public CanOkHttp addRepeat(@NonNull String key, @NonNull String value) {


        repeatMap.put(checkString(key), checkString(value));

        return this;
    }

    /**
     * 添加json参数
     *
     * @param key   键
     * @param value 值
     * @return CanOkHttp
     */
    public CanOkHttp addJSON(@NonNull String key, @NonNull Object value) {

        jsonMap.put(key, value);

        return this;
    }


    /**
     * 添加参数
     *
     * @param map 键
     * @return CanOkHttp
     */
    public CanOkHttp addMap(Map<String, String> map) {

        paramMap.putAll(map);


        return this;
    }


    /**
     * 添加头部参数
     *
     * @param key   键
     * @param value 值
     * @return CanOkHttp
     */
    public CanOkHttp addHeader(@NonNull String key, @NonNull String value) {

        headerMap.put(checkString(key), checkString(value));
        return this;
    }

    /**
     * 设置url
     *
     * @param url 地址
     * @return CanOkHttp
     */
    public CanOkHttp url(@NonNull String url) {


        this.url = url;
        if (TextUtils.isEmpty(this.host)) {
            this.host = getIP(url);
        }


        return this;
    }


    private String getIP(String url) {

        try {
            URI uri = URI.create(url);
            URI effectiveURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
            return effectiveURI.toString();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * post方法
     *
     * @return CanOkHttp
     */
    public CanOkHttp post() {

        return post(0);
    }

    /**
     * post方法
     *
     * @param publicType 是否添加公共参数
     * @return CanOkHttp
     */
    public CanOkHttp post(int publicType) {

        isPost = true;


        boolean isPublic = false;

        switch (publicType) {
            case 0:
                isPublic = mCurrentConfig.getPublicType() == 2 || mCurrentConfig.getPublicType() == 3;
                break;
            case 1:
                isPublic = true;
                break;
            case 2:
                isPublic = false;
                break;
        }
        tempPublicType = publicType;
        try {
            mRequest = fetchRequest(true, isPublic);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!isInitOkClient) {
            initClient();
        }

        return this;
    }


    /**
     * get 方法
     *
     * @return CanOkHttp
     */
    public CanOkHttp get() {


        return get(0);
    }


    /**
     * get 方法
     *
     * @param publicType 是否添加公共参数 0 使用全局设置 1强制使用 2强制关闭
     * @return CanOkHttp
     */
    public CanOkHttp get(int publicType) {

        isPost = false;


        boolean isPublic = false;

        switch (publicType) {
            case 0:
                isPublic = mCurrentConfig.getPublicType() == 1 || mCurrentConfig.getPublicType() == 3;
                break;
            case 1:
                isPublic = true;
                break;
            case 2:
                isPublic = false;
                break;
        }
        tempPublicType = publicType;

        try {
            mRequest = fetchRequest(false, isPublic);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (!isInitOkClient) {
            initClient();
        }

        return this;
    }


    /**
     * 设置连接超时
     *
     * @param connectTimeout 超时时间 单位秒
     * @return CanOkHttp
     */
    public CanOkHttp setConnectTimeout(int connectTimeout) {

        mCurrentConfig.setConnectTimeout(connectTimeout);
        return this;
    }

    /**
     * 设置读出超时
     *
     * @param readTimeout 超时时间 单位秒
     * @return CanOkHttp
     */
    public CanOkHttp setReadTimeout(int readTimeout) {
        mCurrentConfig.setReadTimeout(readTimeout);
        return this;
    }

    /**
     * 设置写入超时
     *
     * @param writeTimeout 超时时间 单位秒
     * @return CanOkHttp
     */
    public CanOkHttp setWriteTimeout(int writeTimeout) {
        mCurrentConfig.setWriteTimeout(writeTimeout);
        return this;
    }

    public CanOkHttp setDownAccessFile(boolean downAccessFile) {
        mCurrentConfig.setDownAccessFile(downAccessFile);
        return this;
    }

    /**
     * 设置tag，用来取消请求的标识，一般用当前的activity
     *
     * @param obj 一般用当前的activity
     * @return CanOkHttp
     */
    public CanOkHttp setTag(@NonNull Object obj) {

        mCurrentConfig.setTag(obj);

        return this;
    }

    /**
     * @param isApplicationJson 是否是Application/Json
     * @return CanOkHttp
     */
    public CanOkHttp setApplicationJson(boolean isApplicationJson) {

        mCurrentConfig.setApplicationJson(isApplicationJson);

        return this;
    }


    /**
     * @param otherMethod 设置请求方式
     * @return CanOkHttp
     */
    public CanOkHttp setOtherMethod(String otherMethod) {

        mCurrentConfig.setOtherMethod(otherMethod);

        return this;
    }


    /**
     * 设置cache类型
     *
     * @param cacheType cache类型
     * @return CanOkHttp
     */
    public CanOkHttp setCacheType(@CacheType int cacheType) {
        mCurrentConfig.setCacheType(cacheType);
        return this;
    }


    /**
     * https失败后是否重试
     *
     * @param httpsTry boolean
     * @return CanOkHttp
     */
    public CanOkHttp setHttpsTry(boolean httpsTry) {
        mCurrentConfig.setHttpsTry(httpsTry);
        return this;
    }

    /**
     * https失败后重试类型
     *
     * @param httpsTryType int
     * @return CanOkHttp
     */
    public CanOkHttp setHttpsTryType(int httpsTryType) {
        mCurrentConfig.setHttpsTryType(httpsTryType);
        return this;
    }

    /**
     * 设置多长时间内只读取缓存
     *
     * @param cacheNoHttpTime 不请求网络的时间
     * @return CanOkHttp
     */
    public CanOkHttp setCacheNoHttpTime(int cacheNoHttpTime) {

        mCurrentConfig.setCacheNoHttpTime(cacheNoHttpTime);

        return this;
    }

    public CanOkHttp setUseClientType(int useClientType) {
        mCurrentConfig.setUseClientType(useClientType);

        return this;
    }


    /**
     * 设置缓存存活时间
     *
     * @param cacheSurvivalTime 缓存存活时间，应比cacheNoHttpTime大
     * @return CanOkHttp
     */
    public CanOkHttp setCacheSurvivalTime(int cacheSurvivalTime) {


        mCurrentConfig.setCacheSurvivalTime(cacheSurvivalTime);
        return this;
    }

    /**
     * 设置在线程中读缓存
     *
     * @param isCacheInThread 设置在线程中读缓存
     * @return CanOkHttp
     */
    public CanOkHttp setCacheInThread(boolean isCacheInThread) {


        mCurrentConfig.setCacheInThread(isCacheInThread);
        return this;
    }

    /**
     * 设置当前请求结果打印日志为json格式
     *
     * @param json 是否为json
     * @return CanOkHttp
     */
    public CanOkHttp setJson(boolean json) {
        mCurrentConfig.setJson(json);
        return this;
    }

    /**
     * 是否重试
     *
     * @param retryOnConnectionFailure 是否重试
     * @return CanOkHttp
     */
    public CanOkHttp setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
        mCurrentConfig.setRetryOnConnectionFailure(retryOnConnectionFailure);
        return this;
    }

    /**
     * 重试次数
     *
     * @param maxRetry 重试次数
     * @return CanOkHttp
     */
    public CanOkHttp setMaxRetry(int maxRetry) {
        mCurrentConfig.setMaxRetry(maxRetry);
        return this;
    }


    /**
     * @param dns Dns
     * @return CanOkHttp
     */
    public CanOkHttp setDns(Dns dns) {
        mCurrentConfig.setDns(dns);
        return this;
    }

    /**
     * 是否打开日志拦截
     *
     * @param isOpenLog setOpenLog
     * @return CanOkHttp
     */
    public CanOkHttp setOpenLog(boolean isOpenLog) {
        mCurrentConfig.setOpenLog(isOpenLog);
        return this;
    }

    /**
     * 是否监听上传进度
     *
     * @param isUpDateProgress boolean
     * @return CanOkHttp
     */
    public CanOkHttp setUpDateProgress(boolean isUpDateProgress) {
        mCurrentConfig.setUpLoadProgress(isUpDateProgress);
        return this;
    }

    /**
     * 设置下载状态，在下载中时改为暂停，可停止下载
     *
     * @param downloadStatus 下载状态
     * @return CanOkHttp
     */
    public CanOkHttp setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
        return this;
    }

    /**
     * 设置下载路径
     *
     * @param downloadFileDir 下载路径
     * @return CanOkHttp
     */
    public CanOkHttp setDownloadFileDir(String downloadFileDir) {
        mCurrentConfig.setDownloadFileDir(downloadFileDir);
        return this;
    }

    /**
     * 设置下载是否覆盖
     *
     * @param downCoverFile 是否覆盖
     * @return CanOkHttp
     */
    public CanOkHttp setDownCoverFile(boolean downCoverFile) {
        mCurrentConfig.setDownCoverFile(downCoverFile);
        return this;
    }

    /**
     * 设置CookieJar
     *
     * @param cookieJar CookieJar
     * @return CanOkHttp
     */
    public CanOkHttp setCookieJar(CookieJar cookieJar) {
        mCurrentConfig.setCookieJar(cookieJar);

        return this;
    }

    public CanOkHttp setJsonMap(JSONObject jsonMap) {
        this.jsonMap = jsonMap;
        return this;
    }

    public CanOkHttp putJsonMap(Map<String, Object> map) {
        jsonMap.putAll(map);
        return this;
    }

    /**
     * 下载文件的大小
     *
     * @return 大小
     */
    public long getCompletedSize() {
        return completedSize;
    }

    /**
     * 当前的回调
     *
     * @return CanCallBack
     */
    public CanCallBack getCanCallBack() {
        return mCanCallBack;
    }


    /**
     * 同步执行
     *
     * @return Response
     */
    public Response execute() {

        if (mRequest == null) {
            return null;
        }

        try {

            return mCurrentHttpClient.newCall(mRequest).execute();


        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;
    }

    /**
     * 上传文件
     *
     * @param url       上传地址
     * @param fileParam 文件参数
     * @param filePath  文件路径
     * @param callBack  回调
     */
    public void uploadFile(@NonNull String url, @NonNull String fileParam, @NonNull String filePath, @NonNull final CanCallBack callBack) {


        if (TextUtils.isEmpty(url)) {
            okHttpLog("文件上传接口地址不能为空", false);
            return;
        }

        if (TextUtils.isEmpty(filePath)) {
            okHttpLog("文件地址不能为空", false);
            return;

        }

        isDownOrUpLoad = true;

        FileLoadBean loadBean = new FileLoadBean(url, fileParam, filePath, true);


        upLoadFilePost(loadBean);

        initClient();

        setCallBack(callBack, loadBean);


    }


    /**
     * 上传文件的参数配置
     *
     * @param fileInfo 文件的信息
     */
    private void upLoadFilePost(@NonNull FileLoadBean fileInfo) {

        try {
            String filePath = fileInfo.filePath;
            String interfaceParamName = fileInfo.fileParam;
            String url = fileInfo.url;


            File file = new File(filePath);

            if (!file.exists()) {
                okHttpLog("文件不存在" + filePath, false);
                return;
            }
            MultipartBody.Builder mBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            StringBuilder log = new StringBuilder("PostParams: ");
            log.append(interfaceParamName + "=" + filePath);
            String logInfo;

            if (null != paramMap && !paramMap.isEmpty()) {
                for (String key : paramMap.keySet()) {
                    mBuilder.addFormDataPart(key, paramMap.get(key));
                    logInfo = key + " =" + paramMap.get(key) + ", ";
                    log.append(logInfo);
                }
            }
            okHttpLog(log.toString(), false);
            mBuilder.addFormDataPart(interfaceParamName,
                    file.getName(),
                    RequestBody.create(CanOkHttpUtil.fetchFileMediaType(filePath), file));
            RequestBody requestBody = mBuilder.build();

            mRequest = new Request
                    .Builder()
                    .url(url)
                    .post(mCurrentConfig.isUpLoadProgress() ? new ProgressRequestBody(requestBody, this) : requestBody)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * 下载文件
     *
     * @param url          请求地址
     * @param callBack     回调
     * @param saveFileName 保存名称
     * @return CanOkHttp
     */
    public CanOkHttp startDownload(@NonNull String url, @NonNull final CanCallBack callBack, @NonNull String saveFileName) {

        if (downloadStatus != DownloadStatus.DOWNLOADING) {
            downloadStatus = DownloadStatus.DOWNLOADING;

            isDownOrUpLoad = true;

            FileLoadBean fileInfo = new FileLoadBean(url, mCurrentConfig.getDownloadFileDir(), saveFileName);
            completedSize = fetchCompletedSize(fileInfo);

            addHeader("RANGE", "bytes=" + completedSize + "-");

            setCacheType(CacheType.NETWORK);
            setConnectTimeout(30 * 60);
            setReadTimeout(30 * 60);
            setWriteTimeout(30 * 60);

            url(url);

            get();


            setCallBack(callBack, fileInfo);

        }


        return this;

    }


    /**
     * 设置回调
     *
     * @param callBack 回调
     */
    public void setCallBack(@NonNull CanCallBack callBack) {


        setCallBack(callBack, null);


    }


    /**
     * 设置回调
     *
     * @param callBack CanCallBack
     * @param fileInfo String
     */
    private void setCallBack(@NonNull final CanCallBack callBack, final FileLoadBean fileInfo) {

        mCanCallBack = callBack;

        isDownOrUpLoad = fileInfo != null;

        if (mRequest == null) {


            sendFailMsg(ResultType.FAIL_SOME_WRONG, 0, "mRequest is null");


            return;
        }


        if (!isDownOrUpLoad) {

            if (mCurrentConfig.isCacheInThread() &&
                    (mCurrentConfig.getCacheType() == CacheType.CACHE
                            || mCurrentConfig.getCacheType() == CacheType.CACHE_NETWORK
                            || mCurrentConfig.getCacheType() == CacheType.CACHETIME_NETWORK
                            || mCurrentConfig.getCacheType() == CacheType.CACHETIME_NETWORK_CACHE)) {

                ThreadPool.getInstance().submit(new Job<Boolean>() {

                    @Override
                    public Boolean run() {


                        return dealWithCache(0, "");
                    }
                }, new FutureListener<Boolean>() {
                    @Override
                    public void onFutureDone(Boolean future) {

                        if (future == null || !future) {

                            Message msg = new OkMessage(OkHandler.RUN_ON_UI,
                                    CanOkHttp.this)
                                    .build();
                            OkHandler.getInstance().sendMessage(msg);

                        }


                    }
                });

            } else {


                if (!dealWithCache(0, "")) {
                    doCall(null);
                }


            }


        } else {
            doCall(fileInfo);
        }


    }


    public void doCall(final FileLoadBean fileInfo) {


        Call call = mCurrentHttpClient.newCall(mRequest);


        if (!TextUtils.isEmpty(mCurrentConfig.getTag())) {
            CanCallManager.putCall(mCurrentConfig.getTag(), call);
        }

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {

                    if(mCurrentConfig.getReportError()!=null){

                        mCurrentConfig.getReportError().report(getReportInfo(call),e,0,null);
                    }
                } catch (Throwable exception) {
                    exception.printStackTrace();
                }
//                httpsTryAgain(call, null, e);
                changeLineTryAgain(call, null, e);
            }

            @Override
            public void onResponse(Call call, Response res) throws IOException {


                try {
                    if (res.isSuccessful() && null != res.body()) {


                        if (isDownOrUpLoad) {

                            if (fileInfo != null && fileInfo.isUpLoad) {

                                String str = dealWithRes(res);


                                sendResponseMsg(str);

                            } else {

                                dealDownloadFile(res, fileInfo);
                            }


                        } else {


                            String str = dealWithRes(res);

                            dealWithCache(1, str);
                            sendResponseMsg(str);

                        }

                        if (!TextUtils.isEmpty(mCurrentConfig.getTag()) && call != null) {
                            CanCallManager.cancelCall(mCurrentConfig.getTag(), call);
                        }

                    } else {
                        try {
                            if(mCurrentConfig.getReportError()!=null){
                                mCurrentConfig.getReportError().report(getReportInfo(call),null,res.code(),res.message());
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        changeLineTryAgain(call, res, null);

                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    try {
                        if(mCurrentConfig.getReportError()!=null){
                            mCurrentConfig.getReportError().report(getReportInfo(call),throwable,0,null);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    changeLineTryAgain(call, res, null);
                }


            }
        });
    }


    private void changeLineTryAgain(Call call, Response res, IOException e) {

        boolean changeLine = false;

        if (!isChangeLine && linesMap != null && linesMap.containsKey(this.host)) {

            boolean isNeedTry = true;
            if (!TextUtils.isEmpty(mCurrentConfig.getTag())) {
                isNeedTry = CanCallManager.isHaveTag(mCurrentConfig.getTag());
            }

            if (!TextUtils.isEmpty(mCurrentConfig.getTag()) && call != null) {
                CanCallManager.cancelCall(mCurrentConfig.getTag(), call);
            }

            if (isNeedTry) {
                switch (mCurrentConfig.getHttpsTryType()) {

                    case 0:

                        if (isPost) {
                            changeLine = true;
                            isChangeLine = true;
                            post(tempPublicType);
                            setCallBack(mCanCallBack);
                        } else {
                            changeLine = true;
                            isChangeLine = true;
                            get(tempPublicType);
                            setCallBack(mCanCallBack);
                        }

                        break;

                    case 1:

                        if (!isPost) {
                            changeLine = true;
                            isChangeLine = true;
                            get(tempPublicType);
                            setCallBack(mCanCallBack);
                        }

                        break;

                    case 2:

                        if (isPost) {
                            changeLine = true;
                            isChangeLine = true;
                            post(tempPublicType);
                            setCallBack(mCanCallBack);
                        }

                        break;

                }
            }


        }

        if (!changeLine) {

            httpsTryAgain(call, res, e);
        }


    }

    private void httpsTryAgain(Call call, Response res, IOException e) {
        boolean isHttpsTry = false;
        if (mCurrentConfig.isHttpsTry() && mCanCallBack != null && !TextUtils.isEmpty(url) && url.startsWith("https://")) {

            boolean isNeedTry = true;
            if (!TextUtils.isEmpty(mCurrentConfig.getTag())) {
                isNeedTry = CanCallManager.isHaveTag(mCurrentConfig.getTag());
            }

            if (!TextUtils.isEmpty(mCurrentConfig.getTag()) && call != null) {
                CanCallManager.cancelCall(mCurrentConfig.getTag(), call);
            }
            if (isNeedTry) {
                url = url.replace("https://", "http://");

                switch (mCurrentConfig.getHttpsTryType()) {

                    case 0:

                        if (isPost) {
                            post(tempPublicType);
                            setCallBack(mCanCallBack);
                            isHttpsTry = true;
                        } else {
                            get(tempPublicType);
                            setCallBack(mCanCallBack);
                            isHttpsTry = true;
                        }

                        break;

                    case 1:

                        if (!isPost) {
                            get(tempPublicType);
                            setCallBack(mCanCallBack);
                            isHttpsTry = true;
                        }

                        break;

                    case 2:

                        if (isPost) {
                            post(tempPublicType);
                            setCallBack(mCanCallBack);
                            isHttpsTry = true;
                        }

                        break;

                }

            }


        }

        if (!isHttpsTry) {

            boolean isCache = dealWithCache(2, "");

            if (isDownOrUpLoad || !isCache) {

                if (res != null) {
                    dealWithResponseFail(res);
                } else {
                    dealWithException(e);
                }

            }

            if (!TextUtils.isEmpty(mCurrentConfig.getTag()) && call != null) {
                CanCallManager.cancelCall(mCurrentConfig.getTag(), call);
            }

        }
    }


    /**
     * 处理Response，避免报错
     *
     * @param res Response
     * @return String
     */
    @NonNull
    private String dealWithRes(Response res) throws Throwable {
        String str = "";

        try {
            str = res.body().string();
        } catch (Throwable e) {
            e.printStackTrace();
            str = new String(res.body().bytes(), "utf-8");

        }
        return str;
    }


    /**
     * @param type   0 请求前，1 网络请求成功 2 网络请求失败
     * @param result 请求结果
     * @return 返回TRUE 不请求网络
     */
    private boolean dealWithCache(int type, String result) {


        String cache;

        switch (mCurrentConfig.getCacheType()) {


            case CacheType.NETWORK:


                break;

            case CacheType.CACHE:

                if (type == 0) {
                    ACache aCache = getACache();
                    if (aCache != null) {
                        cache = aCache.getAsString(cache_key);

                        if (!TextUtils.isEmpty(cache)) {

                            sendCacheMsg(cache);

                            return true;
                        }
                    }


                } else if (type == 1) {

                    putCache(result);


                }


                break;


            case CacheType.NET_CACHE:

                if (type == 1) {

                    putCache(result);


                }


                break;


            case CacheType.NETWORK_CACHE:

                if (type == 1) {

                    putCache(result);


                } else if (type == 2) {


                    ACache aCache = getACache();
                    if (aCache != null) {
                        cache = aCache.getAsString(cache_key);


                        if (!TextUtils.isEmpty(cache)) {

                            sendCacheMsg(cache);

                            return true;

                        }
                    }

                }

                break;


            case CacheType.CACHE_NETWORK:

                if (type == 0) {

                    ACache aCache = getACache();
                    if (aCache != null) {
                        cache = aCache.getAsString(cache_key);
                        if (!TextUtils.isEmpty(cache)) {

                            sendCacheMsg(cache);

                        }

                    }

                } else if (type == 1) {

                    putCache(result);


                }

                break;


            case CacheType.CACHETIME_NETWORK:


                if (type == 0) {


                    ACache aCache = getACache();
                    if (aCache != null) {

                        File file = aCache.file(cache_key);


                        if (file != null && file.exists()) {

                            long time = file.lastModified();


                            boolean isCache = (System.currentTimeMillis() - time) / 1000 < mCurrentConfig.getCacheNoHttpTime();

                            if (isCache) {

                                cache = getACache().getAsString(cache_key);


                                if (!TextUtils.isEmpty(cache)) {


                                    sendCacheMsg(cache);

                                    return true;

                                }
                            }

                        }
                    }

                } else if (type == 1) {
                    putCache(result);


                }


                break;

            case CacheType.CACHETIME_NETWORK_CACHE:

                if (type == 0) {
                    ACache aCache = getACache();
                    if (aCache != null) {

                        File file = aCache.file(cache_key);

                        if (file != null && file.exists()) {

                            long time = file.lastModified();


                            boolean isCache = (System.currentTimeMillis() - time) / 1000 < mCurrentConfig.getCacheNoHttpTime();

                            if (isCache) {

                                cache = getACache().getAsString(cache_key);


                                if (!TextUtils.isEmpty(cache)) {

                                    sendCacheMsg(cache);

                                    return true;

                                }
                            }

                        }
                    }


                } else if (type == 1) {

                    putCache(result);


                } else {

                    ACache aCache = getACache();
                    if (aCache != null) {
                        cache = aCache.getAsString(cache_key);


                        if (!TextUtils.isEmpty(cache)) {

                            sendCacheMsg(cache);

                            return true;

                        }
                    }

                }

                break;


        }

        return false;
    }

    private void putCache(String result) {

        ACache aCache = getACache();
        if (aCache == null) {
            return;
        }
        if (mCurrentConfig.getCacheSurvivalTime() <= 0) {

            aCache.put(cache_key, result);
        } else {
            aCache.put(cache_key, result, mCurrentConfig.getCacheSurvivalTime());

        }
    }


    /**
     * 处理下载文件
     *
     * @param res      响应体
     * @param fileInfo 文件信息
     */
    private void dealDownloadFile(Response res, FileLoadBean fileInfo) {


        RandomAccessFile accessFile = null;
        InputStream inputStream = null;
        BufferedInputStream bis = null;
        String filePath = new File(fileInfo.saveFileDir, fileInfo.saveFileName).getAbsolutePath();
        try {
            ResponseBody responseBody = res.body();
            int length;

            accessFile = new RandomAccessFile(new File(fileInfo.saveFileDir, fileInfo.saveFileNameEncrypt).getAbsoluteFile(), "rwd");
            //服务器不支持断点下载时重新下载
            if (TextUtils.isEmpty(res.header("Content-Range"))) {
                completedSize = 0L;

            }
            KLog.e("dealDownloadFile" + completedSize);
            accessFile.seek(completedSize);

            inputStream = responseBody.byteStream();
            byte[] buffer = new byte[2048];
            bis = new BufferedInputStream(inputStream);
            downloadStatus = DownloadStatus.DOWNLOADING;
            while ((length = bis.read(buffer)) > 0 &&
                    (DownloadStatus.DOWNLOADING == downloadStatus)) {
                accessFile.write(buffer, 0, length);
                completedSize += length;
            }

            if (DownloadStatus.PAUSE == downloadStatus) {

                sendFailMsg(-1, -1, "暂停下载");
                return;
            }
            //下载完成
            if (DownloadStatus.DOWNLOADING == downloadStatus) {
                downloadStatus = DownloadStatus.COMPLETED;
                File newFile = new File(fileInfo.saveFileDir, fileInfo.saveFileName);

                if (newFile.exists() && newFile.isFile()) {


                    //处理文件已存在逻辑
                    boolean isSuccess = false;
                    if (mCurrentConfig.isDownCoverFile()) {
                        isSuccess = newFile.delete();
                    }

                    if (!isSuccess || !mCurrentConfig.isDownCoverFile()) {
                        File copyFile = new File(fileInfo.saveFileDir, fileInfo.saveFileNameCopy);
                        newFile.renameTo(copyFile);
                    }

                }
                File oldFile = new File(fileInfo.saveFileDir, fileInfo.saveFileNameEncrypt);
                if (oldFile.exists() && oldFile.isFile()) {
                    boolean isSuccess = oldFile.renameTo(newFile);
                    if (!isSuccess) {
                        filePath = oldFile.getAbsolutePath();
                    }
                }

                sendFileMsg(DownloadStatus.COMPLETED, "下载成功", filePath);
                return;
            }
        } catch (SocketTimeoutException e) {


            sendFailMsg(ResultType.FAIL_WRITE_READ_TIME_OUT, 0, "读写超时");

            return;
        } catch (Throwable e) {

            e.printStackTrace();
            sendFailMsg(ResultType.FAIL_CONNECTION_TIME_OUT, 0, "连接超时");
            return;
        } finally {
            try {
                if (null != bis)
                    bis.close();
                if (null != inputStream)
                    inputStream.close();
                if (null != accessFile)
                    accessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sendFileMsg(DownloadStatus.COMPLETED, "下载成功", filePath);
    }


    /**
     * 处理失败异常
     *
     * @param e IOException
     */
    private void dealWithException(Exception e) {

        String failMessage = "";
        if (e != null && !TextUtils.isEmpty(e.getMessage())) {
            failMessage = e.getMessage();
            okHttpLog(failMessage, false);
        }

        if (!isNetworkAvailable(mApplication)) {

            sendFailMsg(ResultType.FAIL_NO_NETWORK, 0, !TextUtils.isEmpty(failMessage) ? failMessage : "FAIL_NO_NETWORK");
        } else if (e instanceof SocketTimeoutException) {

            if ("timeout".equals(e.getMessage())) {
                sendFailMsg(ResultType.FAIL_WRITE_READ_TIME_OUT, 0, !TextUtils.isEmpty(failMessage) ? failMessage : "FAIL_WRITE_READ_TIME_OUT");

            } else {
                sendFailMsg(ResultType.FAIL_CONNECTION_TIME_OUT, 0, !TextUtils.isEmpty(failMessage) ? failMessage : "FAIL_CONNECTION_TIME_OUT");
            }

        } else if (e instanceof UnknownHostException) {

            sendFailMsg(ResultType.FAIL_URL_ERROR, 0, !TextUtils.isEmpty(failMessage) ? failMessage : "FAIL_UNKNOWN_HOST_ERROR");

        } else if (e instanceof ConnectException) {

            sendFailMsg(ResultType.FAIL_CONNECTION_TIME_OUT, 0, !TextUtils.isEmpty(failMessage) ? failMessage : "FAIL_CONNECTION_TIME_OUT");

        } else if (e instanceof UnknownServiceException) {
            sendFailMsg(ResultType.FAIL_NET_ERROR, 0, !TextUtils.isEmpty(failMessage) ? failMessage : "FAIL_NET_ERROR");

        } else if (e instanceof HttpRetryException) {

            sendFailMsg(ResultType.FAIL_NET_ERROR, 0, !TextUtils.isEmpty(failMessage) ? failMessage : "FAIL_NET_ERROR");
        } else {

            sendFailMsg(ResultType.FAIL_SOME_WRONG, 0, !TextUtils.isEmpty(failMessage) ? failMessage : "FAIL_SOME_WRONG");


        }
    }


    /**
     * 处理Response失败消息
     *
     * @param res Response
     */
    private void dealWithResponseFail(Response res) {
        try {
            if (null != res) {

                okHttpLog("HttpStatus: " + res.code() + " Message:" + res.message(), false);


                int code = res.code();
                switch (code) {

                    case 404:

                        sendFailMsg(ResultType.FAIL_URL_ERROR, code, "FAIL_URL_ERROR");
                        break;

                    case 416:
                        sendFailMsg(ResultType.FAIL_SOME_WRONG, code, "FAIL_SOME_WRONG");
                        break;

                    case 500:
                        sendFailMsg(ResultType.FAIL_NO_RESULT, code, "FAIL_NO_RESULT");
                        break;

                    case 502:
                        sendFailMsg(ResultType.FAIL_NET_ERROR, code, "FAIL_NET_ERROR");
                        break;

                    case 504:
                        sendFailMsg(ResultType.FAIL_CONNECTION_INTERRUPTION, code, "FAIL_CONNECTION_INTERRUPTION");
                        break;

                    default:
                        sendFailMsg(ResultType.FAIL_NET_ERROR, code, "FAIL_NET_ERROR");
                        break;

                }


            }


        } catch (Exception e) {
            e.printStackTrace();
            sendFailMsg(ResultType.FAIL_CONNECTION_INTERRUPTION, 0, "FAIL_CONNECTION_INTERRUPTION");
        } finally {
            if (null != res) {
                res.close();
            }

        }


    }


    /**
     * 得到缓存管理器
     *
     * @return 缓存管理器
     */
    private ACache getACache() {

        try {
            return ACache.get(mCurrentConfig.getCachedDir(), mCurrentConfig.getMaxCacheSize());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }


    /**
     * 获取完整链接
     *
     * @param isPost     boolean
     * @param publicType int
     * @return String
     */
    public String getFullUrl(boolean isPost, int publicType) {
        boolean isGlobal = false;
        if (isPost) {

            switch (publicType) {
                case 0:
                    isGlobal = mCurrentConfig.getPublicType() == 2 || mCurrentConfig.getPublicType() == 3;
                    break;
                case 1:
                    isGlobal = true;
                    break;
                case 2:
                    isGlobal = false;
                    break;
            }
        } else {

            switch (publicType) {
                case 0:
                    isGlobal = mCurrentConfig.getPublicType() == 1 || mCurrentConfig.getPublicType() == 3;
                    break;
                case 1:
                    isGlobal = true;
                    break;
                case 2:
                    isGlobal = false;
                    break;
            }

        }

        String fullUrl = "";
        String currentUrl = url;
        if (!isChangeLine && linesMap != null && linesMap.containsKey(this.host)) {
            ArrayList<String> array = linesMap.get(this.host);
            if (array != null && !array.isEmpty()) {
                currentUrl = url.replace(this.host, array.get((int) (userId % array.size())));
            }

        }

        StringBuilder paramsUrl = new StringBuilder();
        paramsUrl.append(currentUrl);
        if (isPost) {
            FormBody.Builder builder = new FormBody.Builder();

            StringBuilder params = new StringBuilder();

            if (!paramMap.isEmpty()) {

                String logInfo;
                for (String name : paramMap.keySet()) {
                    builder.add(name, paramMap.get(name));

                    if (!TextUtils.isEmpty(url) && url.contains("?")) {
                        logInfo = "&" + name + "=" + paramMap.get(name);
                    } else {
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + name + "=" + paramMap.get(name);
                        } else {
                            logInfo = "&" + name + "=" + paramMap.get(name);
                        }
                    }


                    params.append(logInfo);
                }

            }

            if (!repeatMap.isEmpty()) {

                String logInfo;
                for (String name : repeatMap.keySet()) {
                    builder.add(name, repeatMap.get(name));

                    if (!TextUtils.isEmpty(url) && url.contains("?")) {
                        logInfo = "&" + name + "=" + repeatMap.get(name);
                    } else {
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + name + "=" + repeatMap.get(name);
                        } else {
                            logInfo = "&" + name + "=" + repeatMap.get(name);
                        }
                    }


                    params.append(logInfo);
                }

            }

            if (isGlobal) {
                String timeStamp = mCurrentConfig.getTimeStamp();
                if (!TextUtils.isEmpty(timeStamp)) {
                    String time = String.valueOf(System.currentTimeMillis());
                    builder.add(timeStamp, time);

                    String logInfo;

                    if (!TextUtils.isEmpty(url) && url.contains("?")) {
                        logInfo = "&" + timeStamp + "=" + time;
                    } else {
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + timeStamp + "=" + time;
                        } else {
                            logInfo = "&" + timeStamp + "=" + time;
                        }
                    }

                    params.append(logInfo);
                }

                Map<String, String> map = mCurrentConfig.getGlobalParamMap();
                if (map != null && !map.isEmpty()) {
                    String logInfo;
                    for (String name : map.keySet()) {
                        builder.add(name, map.get(name));
                        if (!TextUtils.isEmpty(url) && url.contains("?")) {
                            logInfo = "&" + name + "=" + map.get(name);
                        } else {
                            if (TextUtils.isEmpty(params.toString())) {
                                logInfo = "?" + name + "=" + map.get(name);
                            } else {
                                logInfo = "&" + name + "=" + map.get(name);
                            }
                        }

                        params.append(logInfo);
                    }

                }
            }

            paramsUrl.append(params);

        } else {

            StringBuilder params = new StringBuilder();
            if (!paramMap.isEmpty()) {
                String logInfo;
                for (String name : paramMap.keySet()) {
                    if (!TextUtils.isEmpty(url) && url.contains("?")) {
                        logInfo = "&" + name + "=" + paramMap.get(name);
                    } else {
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + name + "=" + paramMap.get(name);
                        } else {
                            logInfo = "&" + name + "=" + paramMap.get(name);
                        }
                    }

                    params.append(logInfo);
                }
            }


            if (!repeatMap.isEmpty()) {
                String logInfo;
                for (String name : repeatMap.keySet()) {
                    if (!TextUtils.isEmpty(url) && url.contains("?")) {
                        logInfo = "&" + name + "=" + repeatMap.get(name);
                    } else {
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + name + "=" + repeatMap.get(name);
                        } else {
                            logInfo = "&" + name + "=" + repeatMap.get(name);
                        }
                    }

                    params.append(logInfo);
                }
            }


            if (isGlobal) {

                Map<String, String> map = mCurrentConfig.getGlobalGetParamMap();

                if (map == null) {
                    map = mCurrentConfig.getGlobalParamMap();
                }
                if (map != null && !map.isEmpty()) {
                    String logInfo;
                    for (String name : map.keySet()) {
                        if (!TextUtils.isEmpty(url) && url.contains("?")) {
                            logInfo = "&" + name + "=" + map.get(name);
                        } else {
                            if (TextUtils.isEmpty(params.toString())) {
                                logInfo = "?" + name + "=" + map.get(name);
                            } else {
                                logInfo = "&" + name + "=" + map.get(name);
                            }
                        }

                        params.append(logInfo);
                    }

                }
            }

            paramsUrl.append(params);


        }


        fullUrl = paramsUrl.toString();

        if (!TextUtils.isEmpty(fullUrl) && fullUrl.startsWith("https://")) {
            fullUrl = fullUrl.replace("https://", "http://");
        }

        return fullUrl;

    }


    /**
     * 获取参数
     *
     * @param isPost   是否post
     * @param isGlobal 是否添加全局参数
     * @return Request
     */
    private Request fetchRequest(boolean isPost, boolean isGlobal) {


        String currentUrl = url;
        if (!isChangeLine && linesMap != null && linesMap.containsKey(this.host)) {
            ArrayList<String> array = linesMap.get(this.host);
            if (array != null && !array.isEmpty()) {
                currentUrl = url.replace(this.host, array.get((int) (userId % array.size())));
            }

        }

        Request.Builder requestBuilder = new Request.Builder();

        if (!headerMap.isEmpty()) {
            for (String key : headerMap.keySet()) {
                requestBuilder.addHeader(key, headerMap.get(key));
            }
        }


        if (isGlobal) {
            Map<String, String> globalHeaderMap = mCurrentConfig.getGlobalHeaderMap();
            if (globalHeaderMap != null && !globalHeaderMap.isEmpty()) {
                for (String key : globalHeaderMap.keySet()) {
                    if (!headerMap.containsKey(key)) {
                        requestBuilder.addHeader(key, globalHeaderMap.get(key));
                    }
                }
            }

        }


        StringBuilder paramsUrl = new StringBuilder();
        paramsUrl.append(currentUrl);
        if (isPost) {
            FormBody.Builder builder = new FormBody.Builder();


            StringBuilder params = new StringBuilder();


            if (!paramMap.isEmpty()) {

                String logInfo;
                for (String name : paramMap.keySet()) {
                    builder.add(name, paramMap.get(name));
                    jsonMap.put(name, paramMap.get(name));
                    if (!TextUtils.isEmpty(url) && url.contains("?")) {
                        logInfo = "&" + name + "=" + paramMap.get(name);
                    } else {
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + name + "=" + paramMap.get(name);
                        } else {
                            logInfo = "&" + name + "=" + paramMap.get(name);
                        }
                    }


                    params.append(logInfo);
                }

            }

            if (!repeatMap.isEmpty()) {

                String logInfo;
                for (String name : repeatMap.keySet()) {
                    builder.add(name, repeatMap.get(name));
                    jsonMap.put(name, repeatMap.get(name));
                    if (!TextUtils.isEmpty(url) && url.contains("?")) {
                        logInfo = "&" + name + "=" + repeatMap.get(name);
                    } else {
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + name + "=" + repeatMap.get(name);
                        } else {
                            logInfo = "&" + name + "=" + repeatMap.get(name);
                        }
                    }


                    params.append(logInfo);
                }

            }

            if (isGlobal) {
                String timeStamp = mCurrentConfig.getTimeStamp();
                if (!TextUtils.isEmpty(timeStamp)) {
                    String time = String.valueOf(System.currentTimeMillis());
                    builder.add(timeStamp, time);
                    jsonMap.put(timeStamp, time);
                    String logInfo;

                    if (!TextUtils.isEmpty(url) && url.contains("?")) {
                        logInfo = "&" + timeStamp + "=" + time;
                    } else {
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + timeStamp + "=" + time;
                        } else {
                            logInfo = "&" + timeStamp + "=" + time;
                        }
                    }

                    params.append(logInfo);
                }

                Map<String, String> map = mCurrentConfig.getGlobalParamMap();
                if (map != null && !map.isEmpty()) {
                    String logInfo;
                    for (String name : map.keySet()) {
                        builder.add(name, map.get(name));
                        jsonMap.put(name, map.get(name));
                        if (!TextUtils.isEmpty(url) && url.contains("?")) {
                            logInfo = "&" + name + "=" + map.get(name);
                        } else {
                            if (TextUtils.isEmpty(params.toString())) {
                                logInfo = "?" + name + "=" + map.get(name);
                            } else {
                                logInfo = "&" + name + "=" + map.get(name);
                            }
                        }

                        params.append(logInfo);
                    }

                }
            }

            paramsUrl.append(params);

            okHttpLog(paramsUrl.toString(), false);
            RequestBody requestBody = null;
            if (mCurrentConfig.isApplicationJson()) {
                String jsonStr = jsonMap.toJSONString();
                requestBody = FormBody.create(MediaType.parse("application/json"), jsonStr);
                KLog.json(jsonStr);
            } else {
                requestBody = builder.build();
            }

            requestBuilder = requestBuilder
                    .url(currentUrl)
                    .cacheControl(CacheControl.FORCE_NETWORK);


            String otherMethod = mCurrentConfig.getOtherMethod();
            if (TextUtils.isEmpty(otherMethod)) {
                otherMethod = "POST";
            }
            requestBuilder.method(otherMethod, requestBody);

        } else {

            StringBuilder params = new StringBuilder();
            if (!paramMap.isEmpty()) {
                String logInfo;
                for (String name : paramMap.keySet()) {
                    if (!TextUtils.isEmpty(url) && url.contains("?")) {
                        logInfo = "&" + name + "=" + paramMap.get(name);
                    } else {
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + name + "=" + paramMap.get(name);
                        } else {
                            logInfo = "&" + name + "=" + paramMap.get(name);
                        }
                    }

                    params.append(logInfo);
                }
            }


            if (!repeatMap.isEmpty()) {
                String logInfo;
                for (String name : repeatMap.keySet()) {
                    if (!TextUtils.isEmpty(url) && url.contains("?")) {
                        logInfo = "&" + name + "=" + repeatMap.get(name);
                    } else {
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + name + "=" + repeatMap.get(name);
                        } else {
                            logInfo = "&" + name + "=" + repeatMap.get(name);
                        }
                    }

                    params.append(logInfo);
                }
            }


            if (isGlobal) {

                Map<String, String> map = mCurrentConfig.getGlobalGetParamMap();

                if (map == null) {
                    map = mCurrentConfig.getGlobalParamMap();
                }
                if (map != null && !map.isEmpty()) {
                    String logInfo;
                    for (String name : map.keySet()) {
                        if (!TextUtils.isEmpty(url) && url.contains("?")) {
                            logInfo = "&" + name + "=" + map.get(name);
                        } else {
                            if (TextUtils.isEmpty(params.toString())) {
                                logInfo = "?" + name + "=" + map.get(name);
                            } else {
                                logInfo = "&" + name + "=" + map.get(name);
                            }
                        }

                        params.append(logInfo);
                    }

                }
            }

            paramsUrl.append(params);

            okHttpLog(paramsUrl.toString(), false);

            requestBuilder
                    .url(paramsUrl.toString())
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .get();
        }


        if (Build.VERSION.SDK_INT > 13) {
            requestBuilder.addHeader("Connection", "close");
        }

        cache_key = paramsUrl.toString();

        if (!TextUtils.isEmpty(cache_key) && cache_key.startsWith("https://")) {
            cache_key = cache_key.replace("https://", "http://");
        }

        return requestBuilder.build();

    }


    private long fetchCompletedSize(FileLoadBean fileInfo) {


        String saveFileDir = fileInfo.saveFileDir;
        String saveFileName = fileInfo.saveFileName;
        String url = fileInfo.url;
//        String extension = "";
//        if (url.contains(".")) {
//            extension = url.substring(url.lastIndexOf(".") + 1);//扩展名
//            extension = "." + extension;
//        }


        String saveFileNameCopy = saveFileName + "_copy";

        saveFileDir = TextUtils.isEmpty(saveFileDir) ? mCurrentConfig.getDownloadFileDir() : saveFileDir;
        mkDirNotExists(saveFileDir);
        fileInfo.saveFileDir = saveFileDir;
        fileInfo.saveFileNameCopy = saveFileNameCopy;

        String saveFileNameEncrypt = url;
        try {
            saveFileNameEncrypt = CanOkHttpUtil.MD5StringTo32Bit(url, true);
//            saveFileNameEncrypt += extension;
            fileInfo.saveFileNameEncrypt = saveFileNameEncrypt;
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (!mCurrentConfig.isDownAccessFile()) {
            return 0L;
        }


        File file = new File(saveFileDir, saveFileNameEncrypt);
        if (file.exists() && file.isFile()) {
            long size = file.length();
            okHttpLog("断点文件下载，节点[" + size + "]", false);
            return size;
        }
        return 0L;
    }

    private boolean mkDirNotExists(String dir) {
        File file = new File(dir);

        return file.exists() || file.mkdirs();
    }

    /**
     * 发送失败消息
     *
     * @param code 结果码
     * @param str  失败信息
     */
    private void sendFailMsg(int failCode, int code, String str) {
        downloadStatus = DownloadStatus.INIT;
        okHttpLog("FailCode:" + code + "  FailMessage:" + str, false);
        Message msg = new OkMessage(OkHandler.RESPONSE_FAIL_CALLBACK,
                mCanCallBack,url,
                failCode, code, str)
                .build();
        OkHandler.getInstance().sendMessage(msg);
    }

    /**
     * 发送结果消息
     *
     * @param str 请求结果
     */
    private void sendResponseMsg(String str) {
        okHttpLog(str, true);
        Message msg = new OkMessage(OkHandler.RESPONSE_CALLBACK,
                mCanCallBack,
                str, "")
                .build();
        OkHandler.getInstance().sendMessage(msg);
    }

    /**
     * 发送缓存消息
     *
     * @param str 缓存
     */
    private void sendCacheMsg(String str) {
        okHttpLog(str, true);
        Message msg = new OkMessage(OkHandler.CACHE_CALLBACK,
                mCanCallBack,
                str, "")
                .build();
        OkHandler.getInstance().sendMessage(msg);
    }

    /**
     * 发送文件成功消息
     *
     * @param filePath 文件路径
     */
    private void sendFileMsg(@DownloadStatus int code, String msgStr, String filePath) {
        okHttpLog("DownloadStatus:" + code + "  Msg:" + msgStr + "  filePath:" + filePath, false);
        Message msg = new OkMessage(OkHandler.RESPONSE_FILE_CALLBACK,
                mCanCallBack,url,
                code, msgStr, filePath)
                .build();
        OkHandler.getInstance().sendMessageDelayed(msg, mCurrentConfig.getDownloadDelayTime());
    }


    /**
     * 下载进度
     *
     * @param totalBytesRead 已下载大小
     * @param contentLength  总大小
     * @param isDone         是否完成
     */
    public void sendProgressMsg(long totalBytesRead, long contentLength, boolean isDone) {
        okHttpLog("totalBytesRead:" + totalBytesRead + "  contentLength:" + contentLength + "  isDone:" + isDone, false);
        Message msg = new OkMessage(OkHandler.PROGRESS_CALLBACK,
                mCanCallBack,url,
                totalBytesRead,
                contentLength,
                isDone)
                .build();
        OkHandler.getInstance().sendMessage(msg);
    }


    public String getReportInfo(Call call){
        String method = call.request().method();
        String url = call.request().url().toString();
        String headers = call.request().headers().toString();
        if(!TextUtils.isEmpty(headers)){
            headers =headers.replace("\n","&");
        }

        RequestBody requestBody = call.request().body();
        if(!"GET".equals(method)&&requestBody!=null){
            String body = requestBodyToString(requestBody);
            return "method="+method+",url="+url+",headers="+headers+",body:"+body;
        }else{
            return "method="+method+",url="+url+",headers="+headers;
        }

    }

    private String requestBodyToString(RequestBody requestBody) {
        Buffer buffer = new Buffer();
        try {
            requestBody.writeTo(buffer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return buffer.readUtf8();
    }

    /**
     * 配置okhttp
     *
     * @param application Application
     * @param config      CanConfig
     */
    public static void init(@NonNull Application application, CanConfig config) {

        if (config == null) {

            config = getDefaultConfig(application);

        }

        globalConfig = config;


    }

    /**
     * 获取默认配置
     *
     * @param application Application
     * @return CanConfig
     */
    public static CanConfig getDefaultConfig(@NonNull Application application) {

        CanConfig config = new CanConfig();

        File cacheDir = null;
        String downLoadDir = null;

        try {

            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {

                cacheDir = application.getExternalCacheDir();

                downLoadDir = Environment.getExternalStorageDirectory() + "/DownLoad/";

            }

        } catch (Throwable e) {
            e.printStackTrace();
        }


        if (cacheDir == null) {
            cacheDir = application.getCacheDir();
        }

        if (TextUtils.isEmpty(downLoadDir)) {
            downLoadDir = application.getFilesDir().getAbsolutePath();
            if (!downLoadDir.endsWith(File.separator)) {
                downLoadDir += File.separator;
            }
        }

        config = config.setApplication(application)
                .setJson(false)
                .setMaxCacheSize(50 * 1024 * 1024)
                .setCachedDir(cacheDir)
                .setConnectTimeout(30)
                .setReadTimeout(30)
                .setWriteTimeout(30)
                .setRetryOnConnectionFailure(false)
                .setCacheSurvivalTime(ACache.TIME_DAY)
                .setCacheNoHttpTime(60)
                .setCacheType(CacheType.NETWORK)
                .setCacheInThread(false)
                .setNetworkInterceptors(null)
                .setInterceptors(null)
                .setDownloadFileDir(downLoadDir)
                .setDownloadDelayTime(1000)
                .setDownAccessFile(false)
                .setDownCoverFile(false)
                .setOpenLog(false)
                .setHttpsTry(true)
                .setPublicType(0)
                .setUpLoadProgress(false)
                .setProxy(true)
                .setCookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(application)));

        return config;

    }


    /**
     * 销毁
     */
    public static void destroy() {

        if (globalConfig != null) {
            globalConfig.setApplication(null);
            globalConfig = null;
        }


    }

}
