package com.canyinghao.canokhttp;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

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
import com.canyinghao.canokhttp.threadpool.Future;
import com.canyinghao.canokhttp.threadpool.FutureListener;
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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * CanOkHttp
 *
 * @author canyinghao
 */

public final class CanOkHttp {


    //    全局的config，在Application里执行init得到
    private static CanConfig globalConfig;

    //  当前CanOkHttp持有的config，默认从globalConfig克隆得到
    private CanConfig mCurrentConfig;
    //    应用Application
    private Application mApplication;
    //    当前持有的OkHttpClient
    private OkHttpClient mCurrentHttpClient;
    //   传入所有参数后得到的请求体
    private Request mRequest;
    //  请求参数
    private Map<String, String> paramMap = new HashMap<>();
    //    请求头参数
    private Map<String, String> headerMap = new HashMap<>();

    //  请求地址
    private String url = "";
    //  缓存的key，由url和参数得到
    private String cache_key = "";
    //  回调ui线程中
    private CanCallBack mCanCallBack;
    //  是否是下载或上传
    private boolean isDownOrUpLoad;
    //  已下载的文件大小
    private long completedSize;

    //下载状态
    private int downloadStatus = DownloadStatus.INIT;

    //是否已经初始化OkClient
    private boolean isInitOkClient;


    public static CanOkHttp getInstance() {

        return new CanOkHttp();

    }


    /**
     * 网络是否可用
     *
     * @param context 上下文
     * @return 是否
     */
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo net = cm.getActiveNetworkInfo();

        return net != null && net.getState() == NetworkInfo.State.CONNECTED;
    }

    /**
     * 打印日志 日志开关在Application设置KLog.init(true,"Canyinghao");
     *
     * @param msg 日志信息
     */
    private void okHttpLog(String msg, boolean isResult) {

        if (!isDownOrUpLoad && isResult && mCurrentConfig.isJson()) {
            KLog.json(msg);
        } else {
            KLog.d(msg);
        }


    }

    /**
     * 主机名验证
     */
    private final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * 设置HTTPS认证
     *
     * @param clientBuilder builder
     */
    private void setSslSocketFactory(OkHttpClient.Builder clientBuilder) {
        clientBuilder.hostnameVerifier(DO_NOT_VERIFY);
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            sc.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            clientBuilder.sslSocketFactory(sc.getSocketFactory(), trustManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 日志拦截器
     */
    private Interceptor LOG_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            long startTime = System.currentTimeMillis();
            okHttpLog(String.format("%s-URL: %s %n", chain.request().method(),
                    chain.request().url()), false);
            Response res = chain.proceed(chain.request());
            long endTime = System.currentTimeMillis();
            okHttpLog(String.format("CostTime: %.1fs", (endTime - startTime) / 1000.0), false);
            return res;
        }
    };


    /**
     * 进度拦截器
     */
    private Interceptor PROGRESS_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());

            return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), CanOkHttp.this))
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

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(mCurrentConfig.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(mCurrentConfig.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(mCurrentConfig.getWriteTimeout(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(mCurrentConfig.isRetryOnConnectionFailure());

        if (isDownOrUpLoad) {
            clientBuilder.addNetworkInterceptor(PROGRESS_INTERCEPTOR);
        }
        if (null != mCurrentConfig.getNetworkInterceptors() && !mCurrentConfig.getNetworkInterceptors().isEmpty())
            clientBuilder.networkInterceptors().addAll(mCurrentConfig.getNetworkInterceptors());
        if (null != mCurrentConfig.getInterceptors() && !mCurrentConfig.getInterceptors().isEmpty())
            clientBuilder.interceptors().addAll(mCurrentConfig.getInterceptors());
        clientBuilder.addInterceptor(LOG_INTERCEPTOR);
        setSslSocketFactory(clientBuilder);

        if (null != mCurrentConfig.getCookieJar()) {
            clientBuilder.cookieJar(mCurrentConfig.getCookieJar());
        }

        //实例化client
        mCurrentHttpClient = clientBuilder.build();


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
     * 添加参数
     *
     * @param key   键
     * @param value 值
     * @return CanOkHttp
     */
    public CanOkHttp add(@NonNull String key, @NonNull String value) {


        paramMap.put(key, value);

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

        headerMap.put(key, value);
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
        return this;
    }

    /**
     * post方法
     *
     * @return CanOkHttp
     */
    public CanOkHttp post() {

        return post(false);
    }

    /**
     * post方法
     *
     * @param isPublic 是否添加公共参数
     * @return CanOkHttp
     */
    public CanOkHttp post(boolean isPublic) {

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


        return get(false);
    }


    /**
     * get 方法
     *
     * @param isPublic 是否添加公共参数
     * @return CanOkHttp
     */
    public CanOkHttp get(boolean isPublic) {


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
     * 设置多长时间内只读取缓存
     *
     * @param cacheNoHttpTime 不请求网络的时间
     * @return CanOkHttp
     */
    public CanOkHttp setCacheNoHttpTime(int cacheNoHttpTime) {

        mCurrentConfig.setCacheNoHttpTime(cacheNoHttpTime);

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


        } catch (IOException e) {

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
                    .post(new ProgressRequestBody(requestBody, this))
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


            sendFailMsg(ResultType.FAIL_SOME_WRONG, "mRequest is null");


            return;
        }


        if (!isDownOrUpLoad) {


            ThreadPool.getInstance().submit(new ThreadPool.Job<Boolean>() {

                @Override
                public Boolean run(ThreadPool.JobContext job) {


                    return dealWithCache(0, "");
                }
            }, new FutureListener<Boolean>() {
                @Override
                public void onFutureDone(Future<Boolean> future) {

                    boolean b = future.get();

                    if (b) {
                        return;
                    }

                    doCall(callBack, null);
                }
            });


        } else {
            doCall(callBack, fileInfo);
        }


    }


    private void doCall(@NonNull final CanCallBack callBack, final FileLoadBean fileInfo) {

        if (!isDownOrUpLoad && dealWithCache(0, "")) {
            return;
        }
        Call call = mCurrentHttpClient.newCall(mRequest);


        if (mCurrentConfig.getTag() != null) {
            CanCallManager.putCall(mCurrentConfig.getTag(), call);
        }

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {


                boolean isCache = dealWithCache(2, "");
                if (isDownOrUpLoad || !isCache) {

                    dealWithException(e);
                }

                if (mCurrentConfig.getTag() != null && call != null) {
                    CanCallManager.cancelCall(mCurrentConfig.getTag(), call);
                }


            }

            @Override
            public void onResponse(Call call, Response res) throws IOException {


                if (res.isSuccessful() && null != res.body()) {


                    if (isDownOrUpLoad) {

                        if (fileInfo != null && fileInfo.isUpLoad) {


                            sendResponseMsg(res.body().string());

                        } else {

                            dealDownloadFile(res, fileInfo);
                        }


                    } else {

                        String str = res.body().string();
                        dealWithCache(1, str);
                        sendResponseMsg(str);

                    }


                } else {

                    boolean isCache = dealWithCache(2, "");
                    if (isDownOrUpLoad || !isCache) {

                        dealWithResponseFail(res);

                    }


                }


                if (mCurrentConfig.getTag() != null && call != null) {
                    CanCallManager.cancelCall(mCurrentConfig.getTag(), call);
                }


            }
        });
    }


    /**
     * @param type   0 请求前，1 网络请求成功 2 网络请求失败
     * @param result 请求结果
     * @return 返回TRUE 不请求网络
     */
    private boolean dealWithCache(int type, String result) {


        String cache = "";

        switch (mCurrentConfig.getCacheType()) {


            case CacheType.NETWORK:


                break;

            case CacheType.CACHE:

                if (type == 0) {

                    cache = getACache().getAsString(cache_key);

                    if (!TextUtils.isEmpty(cache)) {

                        sendCacheMsg(cache);

                        return true;
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


                    cache = getACache().getAsString(cache_key);


                    if (!TextUtils.isEmpty(cache)) {

                        sendCacheMsg(cache);

                        return true;

                    }

                }

                break;


            case CacheType.CACHE_NETWORK:

                if (type == 0) {

                    cache = getACache().getAsString(cache_key);


                    if (!TextUtils.isEmpty(cache)) {

                        sendCacheMsg(cache);

                    }


                } else if (type == 1) {

                    putCache(result);


                }

                break;


            case CacheType.CACHETIME_NETWORK:


                if (type == 0) {


                    File file = getACache().file(cache_key);

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


                } else if (type == 1) {
                    putCache(result);


                }


                break;

            case CacheType.CACHETIME_NETWORK_CACHE:

                if (type == 0) {
                    File file = getACache().file(cache_key);

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


                } else if (type == 1) {

                    putCache(result);


                } else {

                    cache = getACache().getAsString(cache_key);


                    if (!TextUtils.isEmpty(cache)) {

                        sendCacheMsg(cache);

                        return true;

                    }

                }

                break;


        }

        return false;
    }

    private void putCache(String result) {
        if (mCurrentConfig.getCacheSurvivalTime() <= 0) {

            getACache().put(cache_key, result);
        } else {
            getACache().put(cache_key, result, mCurrentConfig.getCacheSurvivalTime());

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
        String filePath = mCurrentConfig.getDownloadFileDir() + fileInfo.saveFileName;
        try {
            ResponseBody responseBody = res.body();
            int length;
            accessFile = new RandomAccessFile(fileInfo.saveFileDir + fileInfo.saveFileNameEncrypt, "rwd");
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

                sendFileMsg(DownloadStatus.PAUSE, "暂停下载", filePath);
                return;
            }
            //下载完成
            if (DownloadStatus.DOWNLOADING == downloadStatus) {
                downloadStatus = DownloadStatus.COMPLETED;
                File newFile = new File(fileInfo.saveFileDir, fileInfo.saveFileName);
                //处理文件已存在逻辑
                if (newFile.exists() && newFile.isFile()) {
                    File copyFile = new File(fileInfo.saveFileDir, fileInfo.saveFileNameCopy);
                    boolean isSuccess = newFile.renameTo(copyFile);
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


            sendFailMsg(ResultType.FAIL_WRITE_READ_TIME_OUT, "读写超时");

            return;
        } catch (Exception e) {

            e.printStackTrace();
            sendFailMsg(ResultType.FAIL_CONNECTION_TIME_OUT, "连接超时");
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
    private void dealWithException(IOException e) {

        if (e != null && !TextUtils.isEmpty(e.getMessage())) {
            okHttpLog(e.getMessage(), false);
        }
        if (!isNetworkAvailable(mApplication)) {

            sendFailMsg(ResultType.FAIL_NO_NETWORK, "FAIL_NO_NETWORK");
        } else if (e instanceof SocketTimeoutException) {

            if ("timeout".equals(e.getMessage())) {
                sendFailMsg(ResultType.FAIL_WRITE_READ_TIME_OUT, "FAIL_WRITE_READ_TIME_OUT");

            } else {
                sendFailMsg(ResultType.FAIL_CONNECTION_TIME_OUT, "FAIL_CONNECTION_TIME_OUT");
            }

        } else if (e instanceof UnknownHostException) {

            sendFailMsg(ResultType.FAIL_URL_ERROR, "FAIL_UNKNOWN_HOST_ERROR");

        } else if (e instanceof ConnectException) {

            sendFailMsg(ResultType.FAIL_CONNECTION_TIME_OUT, "FAIL_CONNECTION_TIME_OUT");

        } else if (e instanceof UnknownServiceException) {
            sendFailMsg(ResultType.FAIL_NET_ERROR, "FAIL_NET_ERROR");

        } else if (e instanceof HttpRetryException) {

            sendFailMsg(ResultType.FAIL_NET_ERROR, "FAIL_NET_ERROR");
        } else {


            sendFailMsg(ResultType.FAIL_SOME_WRONG, "FAIL_SOME_WRONG");


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


                switch (res.code()) {

                    case 404:

                        sendFailMsg(ResultType.FAIL_URL_ERROR, "FAIL_URL_ERROR");
                        break;

                    case 416:
                        sendFailMsg(ResultType.FAIL_SOME_WRONG, "FAIL_SOME_WRONG");
                        break;

                    case 500:
                        sendFailMsg(ResultType.FAIL_NO_RESULT, "FAIL_NO_RESULT");
                        break;

                    case 502:
                        sendFailMsg(ResultType.FAIL_NET_ERROR, "FAIL_NET_ERROR");
                        break;

                    case 504:
                        sendFailMsg(ResultType.FAIL_CONNECTION_INTERRUPTION, "FAIL_CONNECTION_INTERRUPTION");
                        break;

                    default:
                        sendFailMsg(ResultType.FAIL_NET_ERROR, "FAIL_NET_ERROR");
                        break;

                }


            }


        } catch (Exception e) {
            e.printStackTrace();
            sendFailMsg(ResultType.FAIL_CONNECTION_INTERRUPTION, "FAIL_CONNECTION_INTERRUPTION");
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

        return ACache.get(mCurrentConfig.getCachedDir(), mCurrentConfig.getMaxCacheSize());


    }


    /**
     * 获取参数
     *
     * @param isPost   是否post
     * @param isGlobal 是否添加全局参数
     * @return Request
     */
    private Request fetchRequest(boolean isPost, boolean isGlobal) {


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
                    requestBuilder.addHeader(key, globalHeaderMap.get(key));
                }
            }
        }


        StringBuilder paramsUrl = new StringBuilder();
        paramsUrl.append(url);
        if (isPost) {
            FormBody.Builder builder = new FormBody.Builder();

            StringBuilder params = new StringBuilder();
            if (!paramMap.isEmpty()) {

                String logInfo;
                for (String name : paramMap.keySet()) {
                    builder.add(name, paramMap.get(name));

                    if (TextUtils.isEmpty(params.toString())) {
                        logInfo = "?" + name + "=" + paramMap.get(name);
                    } else {
                        logInfo = "&" + name + "=" + paramMap.get(name);
                    }

                    params.append(logInfo);
                }

            }

            if (isGlobal) {
                Map<String, String> map = mCurrentConfig.getGlobalParamMap();
                if (map != null && !map.isEmpty()) {
                    String logInfo;
                    for (String name : map.keySet()) {
                        builder.add(name, map.get(name));
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + name + "=" + paramMap.get(name);
                        } else {
                            logInfo = "&" + name + "=" + paramMap.get(name);
                        }
                        params.append(logInfo);
                    }

                }
            }

            paramsUrl.append(params);
            okHttpLog(paramsUrl.toString(), false);
            requestBuilder
                    .url(url)
                    .post(builder.build());
        } else {

            StringBuilder params = new StringBuilder();
            if (!paramMap.isEmpty()) {
                String logInfo;
                for (String name : paramMap.keySet()) {
                    if (TextUtils.isEmpty(params.toString())) {
                        logInfo = "?" + name + "=" + paramMap.get(name);
                    } else {
                        logInfo = "&" + name + "=" + paramMap.get(name);
                    }
                    params.append(logInfo);
                }
            }

            if (isGlobal) {
                Map<String, String> map = mCurrentConfig.getGlobalParamMap();
                if (map != null && !map.isEmpty()) {
                    String logInfo;
                    for (String name : map.keySet()) {
                        if (TextUtils.isEmpty(params.toString())) {
                            logInfo = "?" + name + "=" + paramMap.get(name);
                        } else {
                            logInfo = "&" + name + "=" + paramMap.get(name);
                        }
                        params.append(logInfo);
                    }

                }
            }

            paramsUrl.append(params);

            okHttpLog(paramsUrl.toString(), false);

            requestBuilder
                    .url(paramsUrl.toString())
                    .get();
        }


        if (Build.VERSION.SDK_INT > 13) {
            requestBuilder.addHeader("Connection", "close");
        }

        cache_key = paramsUrl.toString();

        return requestBuilder.build();

    }


    private long fetchCompletedSize(FileLoadBean fileInfo) {


        String saveFileDir = fileInfo.saveFileDir;
        String saveFileName = fileInfo.saveFileName;
        String url = fileInfo.url;
        String extension = "";
        if (url.contains(".")) {
            extension = url.substring(url.lastIndexOf(".") + 1);//扩展名
            extension = "." + extension;
        }


        String saveFileNameCopy = saveFileName + "_copy" + extension;

        saveFileDir = TextUtils.isEmpty(saveFileDir) ? mCurrentConfig.getDownloadFileDir() : saveFileDir;
        mkDirNotExists(saveFileDir);
        fileInfo.saveFileDir = saveFileDir;
        fileInfo.saveFileNameCopy = saveFileNameCopy;

        String saveFileNameEncrypt = url;
        try {
            saveFileNameEncrypt = CanOkHttpUtil.MD5StringTo32Bit(url, true);
            saveFileNameEncrypt += extension;
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
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    /**
     * 发送失败消息
     *
     * @param code 结果码
     * @param str  失败信息
     */
    private void sendFailMsg(int code, String str) {

        okHttpLog("FailCode:" + code + "  FailMessage:" + str, false);
        Message msg = new OkMessage(OkHandler.RESPONSE_FAIL_CALLBACK,
                mCanCallBack,
                code, str)
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
                mCanCallBack,
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
                mCanCallBack,
                totalBytesRead,
                contentLength,
                isDone)
                .build();
        OkHandler.getInstance().sendMessage(msg);
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
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            cacheDir = application.getExternalCacheDir();


            downLoadDir = Environment.getExternalStorageDirectory() + "/DownLoad/";

        }

        if (cacheDir == null) {
            cacheDir = application.getCacheDir();
        }

        if (TextUtils.isEmpty(downLoadDir)) {
            downLoadDir = application.getFilesDir().getAbsolutePath();
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
                .setNetworkInterceptors(null)
                .setInterceptors(null)
                .setDownloadFileDir(downLoadDir)
                .setDownloadDelayTime(1000)
                .setDownAccessFile(false)
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
