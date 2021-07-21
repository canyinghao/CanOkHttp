package com.canyinghao.canokhttp.demo;

import android.app.Application;
import android.util.Log;

import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.threadpool.ThreadPool;
import com.socks.library.KLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by canyinghao on 2016/10/13.
 */

public class App extends Application implements
        Thread.UncaughtExceptionHandler{

    @Override
    public void onCreate() {
        super.onCreate();
        Map<String,String> some = new HashMap<>();
        some.put("test","test");
        some.put("test1","test1");

        Map<String,String> someget = new HashMap<>();
        someget.put("test2","test2");
        someget.put("test3","test3");

        CanOkHttp.init(this,CanOkHttp.getDefaultConfig(this).setJson(true)
                .setRetryOnConnectionFailure(true)
                .setDownloadDelayTime(1000)
                .setPublicType(3)
                .setUseClientType(3)
                .setDownAccessFile(true)
                .setGlobalParamMap(some)
                .setGlobalGetParamMap(someget)
                .setProxy(false)
                .setMaxRetry(2).setHttpsTry(true));

        KLog.init(true,"Canyinghao");

        ThreadPool.initIoSchedulerHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void onTerminate() {
        CanOkHttp.destroy();
        super.onTerminate();
    }



    private String getCrashReport(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String expcetionStr = sw.toString();
        try {
            sw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pw.close();
        return expcetionStr;
    }

    private static final String FILE_NAME = "failLog.txt";

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        String eStr = getCrashReport(ex);


        Log.e("APP", eStr);


        try {


            File file = new File(getExternalCacheDir(), FILE_NAME);


            boolean isCreate = !file.exists() && file.createNewFile();


            if (isCreate) {
                FileOutputStream f = new FileOutputStream(
                        file);
                f.write(eStr.getBytes());
                f.close();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }


        System.exit(0);
    }
}
