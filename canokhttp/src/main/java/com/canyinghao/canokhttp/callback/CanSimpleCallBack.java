package com.canyinghao.canokhttp.callback;

import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.annotation.ResultType;

/**
 * CanSimpleCallBack
 *
 * @author canyinghao
 */
public class CanSimpleCallBack implements CanCallBack {


    @Override
    public void onCache(Object result) {

    }

    @Override
    public void onResponse(Object result) {

    }

    @Override
    public void onFailure(@ResultType int type, String e) {

    }

    @Override
    public void onFileSuccess(@DownloadStatus int status, String msg, String filePath) {

    }

    @Override
    public void onProgress(long bytesRead, long contentLength, boolean done) {

    }
}
