package com.canyinghao.canokhttp.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.annotation.ResultType;
import com.canyinghao.canokhttp.queue.CanFileGlobalCallBack;
import com.canyinghao.canokhttp.queue.DownFileUtils;
import com.canyinghao.canokhttp.queue.DownloadManager;
import com.socks.library.KLog;

import java.io.File;

/**
 * Created by canyinghao on 2017/1/9.
 */

public class WebActivity extends BaseActivity  {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = new WebView(this);

        setContentView(webView);


        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                return false;
            }

        });


        final DownloadManager downloadManager = new DownloadManager(getApplicationContext());

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, final String mimetype,
                                        long contentLength) {

                final DownloadManager.Request request=   new DownloadManager.Request(url,"/sdcard/");


                downloadManager.enqueue(request, new CanFileGlobalCallBack() {
                    @Override
                    public void onFailure(String url,@ResultType int type, int code, String e) {
                        KLog.e("onFailure");
                    }

                    @Override
                    public void onFileSuccess(String url,@DownloadStatus int status, String msg, String filePath) {
                        KLog.e("onFileSuccess");
                        KLog.e(DownFileUtils.getMimeType(filePath));

//                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                        intent.setDataAndType(Uri.fromFile(new File(filePath)), DownFileUtils.getMimeType(filePath));
//                        startActivity(intent);

                    }

                    @Override
                    public void onProgress(String url,long bytesRead, long contentLength, boolean done) {

                    }

                    @Override
                    public void onDowning(String url) {
                        KLog.e("onDowning");
                        Toast.makeText(getApplicationContext(),"downing",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDownedLocal(String url, String filePath) {
                        KLog.e("onDownedLocal");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(filePath)), DownFileUtils.getMimeType(filePath));
                        startActivity(intent);

                    }
                });


            }
        });


        webView.loadUrl("http://app.bilibili.com/");
    }
}
