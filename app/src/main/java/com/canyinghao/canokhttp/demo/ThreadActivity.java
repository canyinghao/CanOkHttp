package com.canyinghao.canokhttp.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.canyinghao.canokhttp.threadpool.Future;
import com.canyinghao.canokhttp.threadpool.FutureListener;
import com.canyinghao.canokhttp.threadpool.Job;
import com.canyinghao.canokhttp.threadpool.JobContext;
import com.canyinghao.canokhttp.threadpool.ThreadPool;
import com.canyinghao.canokhttp.threadpool.WorkerManager;
import com.socks.library.KLog;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yangjian on 2016/11/14.
 */

public class ThreadActivity extends BaseActivity {


    @Bind(R.id.btn_1)
    Button btn1;
    @Bind(R.id.tv_result)
    TextView tvResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        ButterKnife.bind(this);

        ThreadPool.getInstance().setIdleCallback(new Runnable() {
            @Override
            public void run() {

                KLog.e("all worker finish");
            }
        });
    }

    @OnClick({R.id.btn_1,R.id.btn_2})
    public void  click(View v){

        switch (v.getId()){
            case R.id.btn_1:


                for(int i=0;i<10;i++){
                    ThreadPool.getInstance().setTag("work"+i).submit(new Job<Object>() {
                        @Override
                        public Object run(JobContext var1) {

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            KLog.e(var1.getTag()+var1.isCanceled());


                            return var1.getTag();
                        }
                    }, new FutureListener<Object>() {
                        @Override
                        public void onFutureDone(Future<Object> future) {

                            KLog.e("onFutureDone" +future.get().toString());


                        }
                    });
                }



                break;

            case R.id.btn_2:

                WorkerManager.cancelCallByTag("work1");
                WorkerManager.cancelCallByTag("work5");

                break;
        }
    }
}