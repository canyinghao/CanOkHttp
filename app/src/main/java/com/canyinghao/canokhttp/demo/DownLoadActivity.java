package com.canyinghao.canokhttp.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.annotation.ResultType;
import com.canyinghao.canokhttp.callback.CanFileCallBack;
import com.canyinghao.canokhttp.queue.DownloadManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by canyinghao on 2016/10/13.
 */

public class DownLoadActivity extends BaseActivity {


    @Bind(R.id.downloadProgress)
    ProgressBar downloadProgress;
    @Bind(R.id.tvResult)
    TextView tvResult;

//    private String url = "http://downmp413.ffxia.com/mp413/%E7%8E%8B%E5%AD%90%E6%96%87-%E7%94%9F%E5%A6%82%E5%A4%8F%E8%8A%B1[68mtv.com].mp4";
//    private String url = "http://d.yx934.com/yx934/425YX/JUEZHANPINGANJINGJHJC/JUEZHANPINGANJINGJHJC_1074033.apk";
    private String url = "https://raw.githubusercontent.com/linglongxin24/DylanStepCount/master/app-debug.apk";




    CanOkHttp okHttp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.btn_1,R.id.btn_2,R.id.btn_3})
    public void click(View v){


        switch (v.getId()){

            case R.id.btn_1:

//                startDownLoad();

                startDown();

                break;

            case R.id.btn_2:

                if(okHttp!=null){

                    okHttp.setDownloadStatus(DownloadStatus.PAUSE);
                }

                break;

            case R.id.btn_3:
                startDownLoad();
                break;


        }


    }

    private void startDownLoad(){

        CanFileCallBack callBack = new CanFileCallBack() {
            @Override
            public void onFailure(@ResultType int type,int code, String e) {



            }

            @Override
            public void onFileSuccess(@DownloadStatus int status, String msg, String filePath) {

                tvResult.setText(msg+"  "+filePath);
            }

            @Override
            public void onProgress(long bytesRead, long contentLength, boolean done) {

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
                    .setDownloadFileDir(getExternalFilesDir("download").getAbsolutePath())
                    .setDownCoverFile(true)
                    .startDownload(url, callBack, "canyinghao.apk");
        }else{
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

        String url = "http://apkdown.zymk.cn/api/ChannelName/GetApkByChanne?packName=com.comic.manhuatai&channeCode=360";

        DownloadManager.Request request = new DownloadManager.Request(url, downDir);
        request.setNotificationVisibility(true);


        if(downloadManager==null){
            downloadManager = new DownloadManager(getApplicationContext());
        }

        downloadManager.enqueue(request, null);
    }

}
