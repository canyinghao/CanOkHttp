package com.canyinghao.canokhttp.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.annotation.CacheType;
import com.canyinghao.canokhttp.callback.CanCacheCallBack;
import com.socks.library.KLog;

/**
 * Created by jianyang on 2016/11/3.
 */

public class TestActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        findViewById(R.id.test)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {



                        for(int i=0;i<1000;i++){
                            quest(i);
                        }
                    }
                });


    }



    private void quest(int index){


        CanOkHttp.getInstance()
                .setCacheType(CacheType.CACHE_NETWORK)
                .url("https://www.baidu.com?index="+index)
                .get()
                .setCallBack(new CanCacheCallBack() {
                    @Override
                    public void onCache(Object result) {

                        KLog.e("onCache");
                    }

                    @Override
                    public void onResponse(Object result) {

                        KLog.e("onResponse");

                    }

                    @Override
                    public void onFailure(int failCode,int code, String e) {

                        KLog.e("onFailure");


                    }


                });




    }
}
