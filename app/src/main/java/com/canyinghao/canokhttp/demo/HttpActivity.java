package com.canyinghao.canokhttp.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by canyinghao on 2016/10/13.
 */

public class HttpActivity extends BaseActivity {

    @Bind(R.id.btn_1)
    AppCompatButton btn1;
    @Bind(R.id.btn_2)
    AppCompatButton btn2;
    @Bind(R.id.btn_3)
    AppCompatButton btn3;
    @Bind(R.id.btn_4)
    AppCompatButton btn4;
    @Bind(R.id.btn_5)
    AppCompatButton btn5;
    @Bind(R.id.btn_6)
    AppCompatButton btn6;
    @Bind(R.id.tv_result)
    TextView tvResult;

    public String url = "https://api.k780.com:88/some/";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);
        ButterKnife.bind(this);

        Map<String,ArrayList<String>> map = new HashMap<>();
        ArrayList<String>  temp = new ArrayList<>();
        temp.add("http://apii.k780.com/");
        map.put("https://api.k780.com:88/",temp);
        CanOkHttp.setLinesMap(map);



    }


    @OnClick({R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6})
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
