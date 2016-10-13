package com.canyinghao.canokhttp.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.annotation.ResultType;
import com.canyinghao.canokhttp.callback.CanCallBack;
import com.socks.library.KLog;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by canyinghao on 2016/10/13.
 */

public class MainActivity extends BaseActivity {

    @Bind(R.id.btn_1)
    AppCompatButton btn1;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.btn_1,R.id.btn_2,R.id.btn_3})
    public void click(View v){

        switch (v.getId()){

            case R.id.btn_1:


                startActivity(new Intent(this,HttpActivity.class));


                break;

            case R.id.btn_2:


                startActivity(new Intent(this,DownLoadActivity.class));

                break;

            case R.id.btn_3:

                startActivity(new Intent(this,UpLoadActivity.class));


                break;




        }
    }
}
