package com.canyinghao.canokhttp.callback;

import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.annotation.ResultType;

/**
 * CanCallBack
 *
 * @author canyinghao
 */
public interface CanCallBack {



    void onCache(Object result);

    void onResponse(Object result);

    void onFailure(String url ,@ResultType int type, int code, String e);

    void onFileSuccess(String url ,@DownloadStatus int status,String msg, String filePath);

    void onProgress(String url ,long bytesRead, long contentLength, boolean done);


}
