package com.canyinghao.canokhttp.queue;

import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.annotation.ResultType;

/**
 * Created by jianyang on 2017/5/31.
 */

public interface CanFileGlobalCallBack {


    void onStart(String url);


    void onFailure(String url, @ResultType int type, int code, String e);


    void onFileSuccess(String url, @DownloadStatus int status, String msg, String filePath);


    void onProgress(String url, long bytesRead, long contentLength, boolean done);

    void onDowning(String url);

    void onDownedLocal(String url,String filePath);
}
