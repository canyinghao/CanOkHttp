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
    public void onFailure(String url ,@ResultType int type,int code, String e) {

    }

    @Override
    public void onFileSuccess(String url ,@DownloadStatus int status, String msg, String filePath) {

    }

    @Override
    public void onProgress(String url ,long bytesRead, long contentLength, boolean done) {

    }
}
