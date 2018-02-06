package com.canyinghao.canokhttp.demo;

import android.app.Application;
import android.util.Log;

import com.canyinghao.canokhttp.CanOkHttp;
import com.socks.library.KLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by canyinghao on 2016/10/13.
 */

public class App extends Application implements
        Thread.UncaughtExceptionHandler{

    @Override
    public void onCreate() {
        super.onCreate();
        CanOkHttp.init(this,CanOkHttp.getDefaultConfig(this).setJson(true)
                .setRetryOnConnectionFailure(true)
                .setDownloadDelayTime(1000)
                .setPublicType(3)
                .setUseClientType(3)
                .setMaxRetry(2).setHttpsTry(true));

        KLog.init(true,"Canyinghao");

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
