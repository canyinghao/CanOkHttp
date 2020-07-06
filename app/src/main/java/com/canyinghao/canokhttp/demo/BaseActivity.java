package com.canyinghao.canokhttp.demo;


import com.canyinghao.canokhttp.CanCallManager;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by canyinghao on 2016/10/13.
 */

public class BaseActivity extends AppCompatActivity {


    @Override
    protected void onDestroy() {
        CanCallManager.cancelCallByActivityDestroy(getClass());

        super.onDestroy();

    }
}
