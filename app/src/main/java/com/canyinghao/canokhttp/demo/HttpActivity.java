package com.canyinghao.canokhttp.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.canyinghao.canokhttp.CanCallManager;
import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.annotation.CacheType;
import com.canyinghao.canokhttp.annotation.ResultType;
import com.canyinghao.canokhttp.callback.CanSimpleCallBack;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;


/**
 * Created by canyinghao on 2016/10/13.
 */

public class HttpActivity extends BaseActivity {

    AppCompatButton btn1;
    AppCompatButton btn2;
    AppCompatButton btn3;
    AppCompatButton btn4;
    AppCompatButton btn5;
    AppCompatButton btn6;
    TextView tvResult;

    public String url = "https://api.k780.com:88/some/";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);

        btn1 = findViewById(R.id.btn_1);
        btn2 = findViewById(R.id.btn_2);
        btn3 = findViewById(R.id.btn_3);
        btn4 = findViewById(R.id.btn_4);
        btn5 = findViewById(R.id.btn_5);
        btn6 = findViewById(R.id.btn_6);
        tvResult = findViewById(R.id.tv_result);

        Map<String,ArrayList<String>> map = new HashMap<>();
        ArrayList<String>  temp = new ArrayList<>();
        temp.add("http://apii.k780.com/");
        map.put("https://api.k780.com:88/",temp);
        CanOkHttp.setLinesMap(map);

        View.OnClickListener clickListener = new View.OnClickListener(){

            @Override
            public void onClick(View view) {
             click(view);
            }
        };

        btn1.setOnClickListener(clickListener);
        btn2.setOnClickListener(clickListener);
        btn3.setOnClickListener(clickListener);
        btn4.setOnClickListener(clickListener);
        btn5.setOnClickListener(clickListener);
        btn6.setOnClickListener(clickListener);


    }


    public void click(View v) {

        tvResult.setText("");

        CanCallManager.cancelCallByActivityDestroy(getClass());

        switch (v.getId()) {

            case R.id.btn_1:

//                仅网络请求（NETWORK）


//                String someUrl  = CanOkHttp.getInstance()
//                        .add("app", "life.time")
//                        .add("appkey", "10003")
//                        .add("sign", "b59bc3ef6191eb9f747dd4e83c99f2a4")
//                        .add("format", "json")
//                        .add(String.valueOf("format"), "json")
//                        .url(url)
//                        .getFullUrl(true,0);


//           KLog.e(someUrl);

                CanOkHttp.getInstance()
                        .add("app", "life.time")
                        .add("appkey", "10003")
                        .add("sign", "b59bc3ef6191eb9f747dd4e83c99f2a4")
                        .add("format", "json")
                        .add(String.valueOf("format"), "json")
                        .url(url)
                        .setTag(this)
                        .setCacheType(CacheType.NETWORK)
                        .get()
                        .setCallBack(new CanSimpleCallBack() {

                            @Override
                            public void onResponse(Object result) {

                                KLog.e("onResponse");
                                tvResult.setText(result.toString());
                            }

                            @Override
                            public void onFailure(@ResultType int type, int code, String e) {

                                KLog.e("onFailure");
                            }


                        });



                break;
            case R.id.btn_2:
//                仅请求一次，直到缓存失效，读取缓存（CACHE）
                CanOkHttp.getInstance()

                        .add("app", "life.time")
                        .add("appkey", "10003")
                        .add("sign", "b59bc3ef6191eb9f747dd4e83c99f2a4")
                        .add("format", "json")
                        .url(url)
                        .setTag(this)
                        .setCacheSurvivalTime(180)
                        .setCacheType(CacheType.CACHE)
                        .post()
                        .setCallBack(new CanSimpleCallBack() {

                            @Override
                            public void onResponse(Object result) {

                                KLog.e("onResponse");

                                tvResult.setText(result.toString());
                            }

                            @Override
                            public void onCache(Object result) {

                                KLog.e("onCache");

                                tvResult.setText(result.toString());
                            }

                            @Override
                            public void onFailure(@ResultType int type, int code, String e) {


                            }


                        });

                break;

            case R.id.btn_3:
//                请求网络失败后读缓存（NETWORK_CACHE）

                CanOkHttp.getInstance()

                        .add("app", "life.time")
                        .add("appkey", "10003")
                        .add("sign", "b59bc3ef6191eb9f747dd4e83c99f2a4")
                        .add("format", "json")
                        .add("cache", "NETWORK_CACHE")
                        .url(url)
                        .setTag(this)
                        .setCacheSurvivalTime(180)
                        .setCacheType(CacheType.NETWORK_CACHE)
                        .post()
                        .setCallBack(new CanSimpleCallBack() {

                            @Override
                            public void onCache(Object result) {
                                KLog.e("onCache");


                                tvResult.setText(result.toString());
                            }

                            @Override
                            public void onResponse(Object result) {

                                KLog.e("onResponse");

                                tvResult.setText(result.toString());
                            }

                            @Override
                            public void onFailure(@ResultType int type, int code, String e) {


                            }


                        });

                break;

            case R.id.btn_4:


                //                读缓存后请求网络（CACHE_NETWORK）

                CanOkHttp.getInstance()

                        .add("app", "life.time")
                        .add("appkey", "10003")
                        .add("sign", "b59bc3ef6191eb9f747dd4e83c99f2a4")
                        .add("format", "json")
                        .add("cache", "CACHE_NETWORK")
                        .url(url)
                        .setTag(this)
                        .setCacheInThread(true)
                        .setCacheSurvivalTime(180)
                        .setCacheType(CacheType.CACHE_NETWORK)
                        .post()
                        .setCallBack(new CanSimpleCallBack() {

                            @Override
                            public void onCache(Object result) {


                                tvResult.setText(result.toString());
                            }

                            @Override
                            public void onResponse(Object result) {


                                tvResult.setText(result.toString());
                            }

                            @Override
                            public void onFailure(@ResultType int type, int code, String e) {


                            }


                        });

                break;
            case R.id.btn_5:

                //   缓存不请求网络的时间之内仅读缓存，过期请求网络（CACHETIME_NETWORK）

                CanOkHttp.getInstance()

                        .add("app", "life.time")
                        .add("appkey", "10003")
                        .add("sign", "b59bc3ef6191eb9f747dd4e83c99f2a4")
                        .add("format", "json")
                        .add("cache", "CACHETIME_NETWORK")
                        .url(url)
                        .setTag(this)
                        .setCacheSurvivalTime(180)
                        .setCacheNoHttpTime(60)
                        .setCacheInThread(true)
                        .setCacheType(CacheType.CACHETIME_NETWORK)
                        .post()
                        .setCallBack(new CanSimpleCallBack() {

                            @Override
                            public void onCache(Object result) {


                                tvResult.setText(result.toString());
                            }

                            @Override
                            public void onResponse(Object result) {


                                tvResult.setText(result.toString());
                            }

                            @Override
                            public void onFailure(@ResultType int type, int code, String e) {


                            }


                        });


                break;


            case R.id.btn_6:

                //   缓存不请求网络的时间之内仅读缓存，过期请求网络，请求失败读取缓存（CACHETIME_NETWORK_CACHE）


                CanOkHttp.getInstance()

                        .add("app", "life.time")
                        .add("appkey", "10003")
                        .add("sign", "b59bc3ef6191eb9f747dd4e83c99f2a4")
                        .add("format", "json")
                        .add("cache", "CACHETIME_NETWORK_CACHE")
                        .url(url)
                        .setTag(this)
                        .setCacheSurvivalTime(0)
                        .setCacheNoHttpTime(60)
                        .setCacheInThread(true)
                        .setCacheType(CacheType.CACHETIME_NETWORK_CACHE)
                        .post()
                        .setCallBack(new CanSimpleCallBack() {

                            @Override
                            public void onCache(Object result) {

                                KLog.e("onCache");

                                tvResult.setText(result.toString());
                            }

                            @Override
                            public void onResponse(Object result) {
                                KLog.e("onResponse");


                                tvResult.setText(result.toString());
                            }

                            @Override
                            public void onFailure(@ResultType int type, int code, String e) {


                            }


                        });


                break;


        }


    }
}
