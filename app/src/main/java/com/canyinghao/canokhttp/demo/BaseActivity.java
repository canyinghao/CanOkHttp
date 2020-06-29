package com.canyinghao.canokhttp.demo;

import android.support.v7.app.AppCompatActivity;

import com.canyinghao.canokhttp.CanCallManager;
import com.socks.library.KLog;

/**
 * Created by canyinghao on 2016/10/13.
 */

public class BaseActivity extends AppCompatActivity {


    @Override
    protected void onDestroy() {
        KLog.e(toString());
        CanCallManager.cancelCallByActivityDestroy(this);

        super.onDestroy();

    }
}
