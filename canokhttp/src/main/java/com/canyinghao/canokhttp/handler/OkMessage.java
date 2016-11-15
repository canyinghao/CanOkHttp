package com.canyinghao.canokhttp.handler;

import android.os.Message;

import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.callback.CanCallBack;

import java.io.Serializable;

/**
 * Created by jianyang on 2016/10/13.
 */

public class OkMessage implements Serializable {


    public int what;
    public CanCallBack canCallBack;
    public String resStr;
    public String filePath;

    public int failCode;

    public String failMsg;

    public long bytesWritten;
    public long contentLength;
    public boolean done;

    public CanOkHttp okHttp;


    public OkMessage(int what, CanCallBack canCallBack,
                     long bytesWritten, long contentLength, boolean done) {
        this.what = what;
        this.canCallBack = canCallBack;

        this.bytesWritten = bytesWritten;
        this.contentLength = contentLength;
        this.done = done;


    }

    public OkMessage(int what, CanCallBack canCallBack, String resStr, String filePath) {
        this.what = what;
        this.canCallBack = canCallBack;

        this.resStr = resStr;

        this.filePath = filePath;
    }


    public OkMessage(int what, CanCallBack canCallBack, int failCode, String failMsg) {
        this.what = what;
        this.canCallBack = canCallBack;

        this.failCode = failCode;
        this.failMsg = failMsg;

    }


    public OkMessage(int what, CanCallBack canCallBack, int failCode, String failMsg,String filePath) {
        this.what = what;
        this.canCallBack = canCallBack;

        this.failCode = failCode;
        this.failMsg = failMsg;
        this.filePath =filePath;

    }

    public OkMessage(int what,CanOkHttp okHttp) {
        this.okHttp = okHttp;
        this.what = what;
    }

    public Message build() {
        Message msg = new Message();
        msg.what = this.what;
        msg.obj = this;
        return msg;
    }


}
