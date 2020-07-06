package com.canyinghao.canokhttp.demo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;


/**
 * Created by yangjian on 2016/11/14.
 */

public class ThreadActivity extends BaseActivity {


    Button btn1;
    TextView tvResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        btn1 = findViewById(R.id.btn_1);
        tvResult = findViewById(R.id.tv_result);

//        ThreadPool.getInstance().setIdleCallback(new Runnable() {
//            @Override
//            public void run() {
//
//                KLog.e("all worker finish");
//            }
//        });
    }

//    @OnClick({R.id.btn_1,R.id.btn_2})
//    public void  click(View v){
//
//        switch (v.getId()){
//            case R.id.btn_1:
//
//
//                for(int i=0;i<10;i++){
//                    ThreadPool.getInstance().setTag("work"+i).submit(new Job<Object>() {
//                        @Override
//                        public Object run(JobContext var1) {
//
//                            try {
//                                Thread.sleep(2000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//
//                            KLog.e(var1.getTag()+var1.isCanceled());
//
//
//                            return var1.getTag();
//                        }
//                    }, new FutureListener<Object>() {
//                        @Override
//                        public void onFutureDone(Future<Object> future) {
//
//                            KLog.e("onFutureDone" +future.get().toString());
//
//
//                        }
//                    });
//                }
//
//
//
//                break;
//
//            case R.id.btn_2:
//
//                WorkerManager.cancelCallByTag("work1");
//                WorkerManager.cancelCallByTag("work5");
//
//                break;
//        }
//    }
}
