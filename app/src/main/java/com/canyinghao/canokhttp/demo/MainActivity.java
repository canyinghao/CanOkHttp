package com.canyinghao.canokhttp.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.socks.library.KLog;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by canyinghao on 2016/10/13.
 */

public class MainActivity extends BaseActivity {

    AppCompatButton btn1;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = findViewById(R.id.btn_1);

        View.OnClickListener clickListener = new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                click(view);
            }
        };


        btn1.setOnClickListener(clickListener);
        findViewById(R.id.btn_2).setOnClickListener(clickListener);
        findViewById(R.id.btn_3).setOnClickListener(clickListener);
        findViewById(R.id.btn_4).setOnClickListener(clickListener);
        findViewById(R.id.btn_5).setOnClickListener(clickListener);
    }

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


            case R.id.btn_4:

                startActivity(new Intent(this,TestActivity.class));


                break;

            case R.id.btn_5:

//                startActivity(new Intent(this,WebActivity.class));

                FormBody.Builder builder = new FormBody.Builder();
                Request.Builder requestBuilder = new Request.Builder();
//                CanOkHttp okHttp =     CanOkHttp.getInstance().url("http://172.16.4.197:8098/discuz/admin.php?action=forums");
                requestBuilder.addHeader("Cookie","kDH2_2132_saltkey=ALl367NC; kDH2_2132_lastvisit=1514872845; kDH2_2132_connect_is_bind=0; kDH2_2132_ulastactivity=e38etRtxy1ZbG6Q95Lvp72MnjA8JXW5U64l4JkyRIZKaFlz7%2FTJ2; kDH2_2132_auth=f8b5ZW9mMiqfljsleIIWIRZ2VAvFLrg%2BM%2FqmVjTHHfxhYnKQp5gFftYt%2Fq2qGyVNoECIbg2uvqFZ4rWshiAQ; kDH2_2132_myrepeat_rr=R0; kDH2_2132_nofavfid=1; kDH2_2132_smile=1D1; kDH2_2132_st_p=1%7C1515643075%7C7e82ac9b61d18573c56bcab269b0712b; kDH2_2132_viewid=tid_391; kDH2_2132_st_t=1%7C1515649096%7Ccf0a23ff1e957484831790ca7ab4968b; kDH2_2132_forum_lastvisit=D_47_1515649066D_2_1515649096; kDH2_2132_visitedfid=2D47; kDH2_2132_sid=rzYk8K; kDH2_2132_lip=172.16.2.110%2C1515650080; kDH2_2132_addoncheck_plugin=1; kDH2_2132_onlineusernum=3; kDH2_2132_sendmail=1; kDH2_2132_lastcheckfeed=1%7C1515651086; kDH2_2132_lastact=1515651172%09admin.php%09")
                        .addHeader("Referer","http://172.16.4.197:8098/discuz/admin.php?action=forums");


                builder.add("formhash","22bb238c")
                        .add("scrolltop","")
                        .add("anchor","")
                        .add("order[1]","0")
                        .add("name[1]","Discuz!")
                        .add("order[2]","0")
                        .add("name[2]","1")
                        .add("order[53]","0")
                        .add("name[53]","2");


                for(int i=0;i<10000;i++){

                    builder.add("neworder[1][]","0").add("newforum[1][]",(i+3)+"");
                    builder.add("newinherited[1][]","");
                }



                builder.add("editsubmit","�ύ");


//                okHttp.post()
//                        .setCallBack(new CanSimpleCallBack());



                OkHttpClient okHttpClient = new OkHttpClient.Builder().build();






                Request  mRequest = requestBuilder
                            .url("http://172.16.4.197:8098/discuz/admin.php?action=forums")
                            .cacheControl(CacheControl.FORCE_NETWORK)
                            .post(builder.build()).build();


                Call call = okHttpClient.newCall(mRequest);


                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        KLog.e(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                    }
                });

                break;



        }
    }
}
