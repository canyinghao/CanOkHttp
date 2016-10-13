package com.canyinghao.canokhttp.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.annotation.ResultType;
import com.canyinghao.canokhttp.callback.CanFileCallBack;
import com.canyinghao.canokhttp.callback.CanSimpleCallBack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;

/**
 * Created by canyinghao on 2016/10/13.
 */

public class UpLoadActivity extends BaseActivity {

    @Bind(R.id.uploadProgress)
    ProgressBar uploadProgress;
    @Bind(R.id.tvFile)
    TextView tvFile;

    @Bind(R.id.tvResult)
    TextView tvResult;

    @Bind(R.id.btn_1)
    AppCompatButton btn1;
    @Bind(R.id.btn_2)
    AppCompatButton btn2;


    private String url = "http://www.canyinghao.com/api/upload";
    private String filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.btn_1, R.id.btn_2})
    public void click(View v) {


        switch (v.getId()) {

            case R.id.btn_1:

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*;image/*");//图片和视频
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);

                break;

            case R.id.btn_2:

                CanOkHttp.getInstance().uploadFile(url, "file", filePath, new CanSimpleCallBack(){

                    @Override
                    public void onResponse(Object result) {

                    }

                    @Override
                    public void onFailure(@ResultType int type, String e) {

                    }


                    @Override
                    public void onProgress(long bytesRead, long contentLength, boolean done) {

                    }

                });


                break;




        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri contentUri = data.getData();
            filePath = FilePathUtil.getFilePathFromUri(this, contentUri);
            if (TextUtils.isEmpty(filePath)) {
                Toast.makeText(this, "获取文件地址失败", Toast.LENGTH_LONG).show();
                return;
            }
            tvFile.setText("上传文件：" + filePath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


//    private void preLogin(){
//
//
//        String pre_login_url = "http://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&su=MTUyNTUxMjY3OTY%3D&rsakt=mod&checkpin=1&client=ssologin.js%28v1.4.18%29&_=1458836718537";
//
//
//        CanOkHttp.getInstance().url(pre_login_url,"").get().setCallBack(new CanSimpleCallBack(){
//
//            @Override
//            public void onResponse(Object result) {
//
//                String pre_response = result.toString();
//                String pre_content_regex = "\\((.*?)/\\)";
//
//                Pattern p =Pattern.compile(pre_content_regex);
//
//                Matcher m =p.matcher(pre_response);
//
//
//
//
//                String pre_content =  m.group(1);
//
//                JSONObject pre_result = JSON.parseObject(pre_content);
//
//
//                String nonce = pre_result.getString("nonce");
//                String pubkey = pre_result.getString("pubkey");
//                String servertime = pre_result.getString("servertime");
//                String  rsakv = pre_result.getString("rsakv");
//
//            }
//
//
//        });
//
//
//
//
//    }


}
