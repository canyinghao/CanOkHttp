package com.canyinghao.canokhttp.demo;

import android.support.v7.app.AppCompatActivity;

import com.canyinghao.canokhttp.CanCallManager;
import com.canyinghao.canokhttp.threadpool.WorkerManager;

/**
 * Created by canyinghao on 2016/10/13.
 */

public class BaseActivity extends AppCompatActivity {


    @Override
    protected void onDestroy() {
        CanCallManager.cancelCallByActivityDestroy(getClass());
        WorkerManager.cancelCallByActivityDestroy(getClass());
        super.onDestroy();

    }
}
