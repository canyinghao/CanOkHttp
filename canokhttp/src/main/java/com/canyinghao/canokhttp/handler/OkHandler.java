package com.canyinghao.canokhttp.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


public class OkHandler extends Handler {


    public static final byte RESPONSE_CALLBACK = 1;
    public static final byte PROGRESS_CALLBACK = 2;
    public static final byte RESPONSE_FILE_CALLBACK = 3;
    public static final byte RESPONSE_FAIL_CALLBACK = 4;
    public static final byte CACHE_CALLBACK = 5;
    public static final byte RUN_ON_UI= 6;


    private static OkHandler singleton;

    public static OkHandler getInstance() {
        if (null == singleton) {
            synchronized (OkHandler.class) {
                if (null == singleton)
                    singleton = new OkHandler();
            }
        }
        return singleton;
    }

    public OkHandler() {
        super(Looper.getMainLooper());
    }


    @Override
    public void handleMessage(Message msg) {
        final int what = msg.what;
        OkMessage callMsg;
        try {
            switch (what) {
                case RESPONSE_CALLBACK:
                    callMsg = (OkMessage) msg.obj;
                    if (null != callMsg.canCallBack) {
                        callMsg.canCallBack.onResponse(callMsg.resStr);
                    }

                    break;
                case PROGRESS_CALLBACK:

                    callMsg = (OkMessage) msg.obj;
                    if (null != callMsg.canCallBack) {
                        callMsg.canCallBack.onProgress(callMsg.url,callMsg.bytesWritten, callMsg.contentLength, callMsg.done);
                    }

                    break;
                case RESPONSE_FILE_CALLBACK:

                    callMsg = (OkMessage) msg.obj;
                    if (null != callMsg.canCallBack) {
                        callMsg.canCallBack.onFileSuccess(callMsg.url,callMsg.failCode, callMsg.failMsg, callMsg.filePath);
                    }

                    break;

                case RESPONSE_FAIL_CALLBACK:
                    callMsg = (OkMessage) msg.obj;
                    if (null != callMsg.canCallBack) {
                        callMsg.canCallBack.onFailure(callMsg.url,callMsg.failCode,callMsg.code, callMsg.failMsg);
                    }
                    break;

                case CACHE_CALLBACK:
                    callMsg = (OkMessage) msg.obj;
                    if (null != callMsg.canCallBack) {
                        callMsg.canCallBack.onCache(callMsg.resStr);
                    }
                    break;


                case RUN_ON_UI:
                    callMsg = (OkMessage) msg.obj;
                    if (null != callMsg.okHttp) {
                        callMsg.okHttp.doCall(null);
                    }
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
