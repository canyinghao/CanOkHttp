package com.canyinghao.canokhttp.queue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class NotificationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        try {
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case "com.ACTION_NOTIFICATION_CLICK":
                        if(intent.hasExtra("url")){
                            String url =intent.getStringExtra("url");
                            DownloadManager.getInstance(context).cancelDownLoad(url);
                        }

                        break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
}