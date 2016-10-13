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

    void onFailure(@ResultType int type, String e);

    void onFileSuccess(@DownloadStatus int status,String msg, String filePath);

    void onProgress(long bytesRead, long contentLength, boolean done);


}
