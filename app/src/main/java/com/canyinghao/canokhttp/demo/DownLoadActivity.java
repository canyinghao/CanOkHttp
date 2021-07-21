package com.canyinghao.canokhttp.demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.annotation.ResultType;
import com.canyinghao.canokhttp.callback.CanFileCallBack;
import com.canyinghao.canokhttp.queue.CanFileGlobalCallBack;
import com.canyinghao.canokhttp.queue.DownFileUtils;
import com.canyinghao.canokhttp.queue.DownloadManager;
import com.socks.library.KLog;

import java.io.File;

import androidx.annotation.Nullable;


/**
 * Created by canyinghao on 2016/10/13.
 */

public class DownLoadActivity extends BaseActivity {



    ProgressBar downloadProgress;

    TextView tvResult;

//    private String url = "http://downmp413.ffxia.com/mp413/%E7%8E%8B%E5%AD%90%E6%96%87-%E7%94%9F%E5%A6%82%E5%A4%8F%E8%8A%B1[68mtv.com].mp4";
//    private String url = "http://d.yx934.com/yx934/425YX/JUEZHANPINGANJINGJHJC/JUEZHANPINGANJINGJHJC_1074033.apk";
    private String url = "https://ecyapk.zymk.cn/apk/com.comic.shenmanhua/2.5.0/com.comic.shenmanhua_2011112233_2.5.0_baidu.apk";




    CanOkHttp okHttp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download);

        tvResult = findViewById(R.id.tvResult);
        downloadProgress = findViewById(R.id.downloadProgress);
        View.OnClickListener onClickListener= new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                click(view);
            }
        };

        findViewById(R.id.btn_1).setOnClickListener(onClickListener);
        findViewById(R.id.btn_2).setOnClickListener(onClickListener);
        findViewById(R.id.btn_3).setOnClickListener(onClickListener);
    }



    public void click(View v){


        switch (v.getId()){

            case R.id.btn_1:

//                startDownLoad();

                startDown();

                break;

            case R.id.btn_2:

//                if(okHttp!=null){
//
//                    okHttp.setDownloadStatus(DownloadStatus.PAUSE);
//                }
                sendBroadcast(new Intent("com.ACTION_NOTIFICATION_CLICK"));

                break;

            case R.id.btn_3:
                startDownLoad();
                break;


        }


    }

    private void startDownLoad(){

        CanFileCallBack callBack = new CanFileCallBack() {
            @Override
            public void onFailure(String url, @ResultType int type,int code, String e) {



            }

            @Override
            public void onFileSuccess(String url, @DownloadStatus int status, String msg, String filePath) {

                tvResult.setText(msg+"  "+filePath);
            }

            @Override
            public void onProgress(String url, long bytesRead, long contentLength, boolean done) {

                int percent = (int) (bytesRead/(float)contentLength *100);
                downloadProgress.setProgress(percent);
                tvResult.setText(percent+"%");

            }
        };


        if(okHttp==null){
            okHttp = CanOkHttp.getInstance()
                    .setConnectTimeout(600)
                    .setReadTimeout(600)
                    .setWriteTimeout(600)
                    .setTag(this)
                    .setDownAccessFile(true)
//                    .addHeader("If-Match","A4C3F40F6AABED2DFB657FB5219BC108")
                    .setDownloadFileDir(getExternalFilesDir("download").getAbsolutePath())
                    .setDownCoverFile(true)
                    .startDownload(url, callBack, "canyinghao.apk");
        }else{
//            okHttp.addHeader("If-Match","A4C3F40F6AABED2DFB657FB5219BC108");
            okHttp.startDownload(url,callBack,"canyinghao.apk");
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        CanCallManager.cancelCallByTag("canyinghao");
    }



    DownloadManager downloadManager;
    private void startDown() {
        String downDir = getExternalCacheDir().getAbsolutePath();
        if (!TextUtils.isEmpty(downDir) && !downDir.endsWith("/")) {
            downDir += "/";
        }

        String[] urls = {"http://d.yx934.com/yx934/425YX/JUEZHANPINGANJINGJHJC/JUEZHANPINGANJINGJHJC_1074033.apk",
                "http://www.canyinghao.com/assets/work/cancalc/cancalc.apk",
                "http://www.canyinghao.com/assets/work/canyinghao/canyinghao.apk"};

        String url = "https://ecyapk.zymk.cn/apk/com.comic.shenmanhua/2.5.0/com.comic.shenmanhua_2011112233_2.5.0_baidu.apk";
//        String url = "https://20bcd96ad202702427d824bd0564da6c.dd.cdntips.com/wxz.myapp.com/16891/026FF4E7DE007D4E7DD1657DC2939498.apk?mkey=5d1705fe7671b558&f=0c6d&fsname=com.wbxm.icartoon_2.5.6_1906272233.apk&hsr=4d5s&cip=118.113.147.173&proto=https";

        DownloadManager.Request request = new DownloadManager.Request(url, downDir);
        request.setNotificationVisibility(true);


        if(downloadManager==null){
            downloadManager = DownloadManager.getInstance(getApplicationContext());
        }

        downloadManager.enqueue(request, new CanFileGlobalCallBack() {
            @Override
            public void onStart(String url) {

            }

            @Override
            public void onFailure(String url, int type, int code, String e) {

            }

            @Override
            public void onFileSuccess(String url, int status, String msg, String filePath) {

            }

            @Override
            public void onProgress(String url, long bytesRead, long contentLength, boolean done) {

                try {
                    KLog.e("bytesRead"+bytesRead);
                    KLog.e("bytesRead:"+((bytesRead/(float)contentLength)*100)+"%");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDowning(String url) {

                Toast.makeText(getApplicationContext(),"正在下载",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownedLocal(String url, String filePath) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(downloadManager.getFileUri(DownLoadActivity.this,new File(filePath)), DownFileUtils.getMimeType(DownLoadActivity.this,filePath));

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                startActivity(intent);

            }
        });
    }

}
