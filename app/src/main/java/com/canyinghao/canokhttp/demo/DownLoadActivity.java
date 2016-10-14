package com.canyinghao.canokhttp.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.annotation.ResultType;
import com.canyinghao.canokhttp.callback.CanFileCallBack;

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

    private String url = "http://downmp413.ffxia.com/mp413/%E7%8E%8B%E5%AD%90%E6%96%87-%E7%94%9F%E5%A6%82%E5%A4%8F%E8%8A%B1[68mtv.com].mp4";
//    private String url = "http://www.canyinghao.com/assets/work/canyinghao/canyinghao.apk";



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

                startDownLoad();

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
            public void onFailure(@ResultType int type, String e) {



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
                    .startDownload(url, callBack, "canyinghao.apk");
        }else{
            okHttp.startDownload(url,callBack,"canyinghao.apk");
        }


    }
}
