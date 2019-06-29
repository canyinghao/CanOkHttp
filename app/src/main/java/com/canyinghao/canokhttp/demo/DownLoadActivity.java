package com.canyinghao.canokhttp.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.canyinghao.canokhttp.queue.DownloadManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by canyinghao on 2016/10/13.
 */

public class DownLoadActivity extends BaseActivity {


    @BindView(R.id.downloadProgress)
    ProgressBar downloadProgress;
    @BindView(R.id.tvResult)
    TextView tvResult;

//    private String url = "http://downmp413.ffxia.com/mp413/%E7%8E%8B%E5%AD%90%E6%96%87-%E7%94%9F%E5%A6%82%E5%A4%8F%E8%8A%B1[68mtv.com].mp4";
//    private String url = "http://d.yx934.com/yx934/425YX/JUEZHANPINGANJINGJHJC/JUEZHANPINGANJINGJHJC_1074033.apk";
    private String url = "http://ecyapk.oss-cn-hangzhou.aliyuncs.com/apk/com.comic.iyouman/1.7.6/com.comic.iyouman_1906062233_1.7.6_tencent.apk";




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

        String url = "http://ecyapk.oss-cn-hangzhou.aliyuncs.com/apk/com.comic.iyouman/1.7.6/com.comic.iyouman_1906062233_1.7.6_tencent.apk?t=5";
//        String url = "https://20bcd96ad202702427d824bd0564da6c.dd.cdntips.com/wxz.myapp.com/16891/026FF4E7DE007D4E7DD1657DC2939498.apk?mkey=5d1705fe7671b558&f=0c6d&fsname=com.wbxm.icartoon_2.5.6_1906272233.apk&hsr=4d5s&cip=118.113.147.173&proto=https";

        DownloadManager.Request request = new DownloadManager.Request(url, downDir);
        request.setNotificationVisibility(true);


        if(downloadManager==null){
            downloadManager = new DownloadManager(getApplicationContext());
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

            }

            @Override
            public void onDowning(String url) {

                Toast.makeText(getApplicationContext(),"正在下载",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownedLocal(String url, String filePath) {

            }
        });
    }

}
