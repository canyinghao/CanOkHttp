package com.canyinghao.canokhttp.callback;

import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.annotation.ResultType;

/**
 * CanCacheCallBack
 *
 * @author canyinghao
 */
public abstract  class CanCacheCallBack implements CanCallBack {


    @Override
    public void onFailure(@ResultType int type,int code, String e) {

    }

    @Override
    public void onFileSuccess(@DownloadStatus int status, String msg, String filePath) {

    }

    @Override
    public void onProgress(long bytesRead, long contentLength, boolean done) {

    }
}
