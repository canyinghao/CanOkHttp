package com.canyinghao.canokhttp.queue;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.R;
import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.annotation.ResultType;
import com.canyinghao.canokhttp.callback.CanFileCallBack;
import com.canyinghao.canokhttp.downpic.SecureHashUtil;
import com.canyinghao.canokhttp.util.CanPreferenceUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

/**
 * DownloadManager
 * Created by canyinghao on 2017/5/31.
 */

public class DownloadManager {

    private Context context;
    private NotificationManager notificationMrg;

    private Map<String, CanFileGlobalCallBack> downMap = new ArrayMap<>();
    private Map<String, Integer> downIdMap = new ArrayMap<>();

    private CanFileGlobalCallBack globalCallBack;


    private int count;

    public DownloadManager(Context context) {
        this.context = context;

    }

    public void setGlobalCallBack(CanFileGlobalCallBack globalCallBack) {
        this.globalCallBack = globalCallBack;
    }

    public CanFileGlobalCallBack getGlobalCallBack() {
        return globalCallBack;
    }

    public void enqueue(final Request request, CanFileGlobalCallBack canFileCallBack) {
        final String url = request.url;

        if (canFileCallBack != null) {

            downMap.put(url, canFileCallBack);
        }


        if (downIdMap.containsKey(url)) {

            if (globalCallBack != null) {
                globalCallBack.onDowning(url);
            }

            if (downMap.containsKey(url)) {

                CanFileGlobalCallBack callBack = downMap.get(url);

                if (callBack != null) {
                    callBack.onDowning(url);
                }
            }

            return;
        }


        String filePath = CanPreferenceUtil.getString(secureHashKey(url), "", context);

        File file = new File(filePath);

        if (file.isFile() && file.exists()) {

            if (globalCallBack != null) {
                globalCallBack.onDownedLocal(url, filePath);
            }

            if (downMap.containsKey(url)) {

                CanFileGlobalCallBack callBack = downMap.get(url);

                if (callBack != null) {
                    callBack.onDownedLocal(url, filePath);
                }
            }

            return;
        }


        String[] splits = url.split("/");
        String fileName = System.currentTimeMillis() + "";
        if (splits.length > 0) {
            String str = splits[splits.length - 1];

            if (!TextUtils.isEmpty(str)) {
                fileName = str;
            }
        }


        downIdMap.put(url, 1000 + count++);

        final String finalFileName = fileName;

        CanOkHttp okHttp = CanOkHttp.getInstance();

        if (request.getRequestHeader() != null && !request.getRequestHeader().isEmpty()) {

            Set<String> set = request.getRequestHeader().keySet();

            for (String key : set) {

                okHttp.addHeader(key, request.getRequestHeader().get(key));
            }
        }
        okHttp.setDownCoverFile(true)
                .startDownload(request.url, new CanFileCallBack() {

                    private long showTime;

                    @Override
                    public void onFailure(@ResultType int type, int code, String e) {


                        if (globalCallBack != null) {
                            globalCallBack.onFailure(url, type, code, e);
                        }


                        if (downMap.containsKey(url)) {

                            CanFileGlobalCallBack callBack = downMap.get(url);

                            if (callBack != null) {
                                callBack.onFailure(url, type, code, e);
                            }
                            downMap.remove(url);
                        }

                        if (request.isNotificationVisibility()) {
                            showDownFailNotify(context, finalFileName, request);
                        }

                        if (downIdMap.containsKey(url)) {
                            downIdMap.remove(url);
                        }

                    }

                    @Override
                    public void onFileSuccess(@DownloadStatus int status, String msg, String filePath) {


                        if (globalCallBack != null) {
                            globalCallBack.onFileSuccess(url, status, msg, filePath);
                        }

                        if (downMap.containsKey(url)) {

                            CanFileGlobalCallBack callBack = downMap.get(url);

                            if (callBack != null) {
                                callBack.onFileSuccess(url, status, msg, filePath);
                            }
                            downMap.remove(url);
                        }


                        CanPreferenceUtil.putString(secureHashKey(url), filePath, context);

                        if (request.isNotificationVisibility()) {
                            File file = new File(filePath);
                            if (file.isFile() && file.exists()) {
                                showDownSuccessNotify(context, finalFileName, filePath, request);
                            } else {
                                hideDownNotify(context, request);
                            }
                        }

                        if (downIdMap.containsKey(url)) {
                            downIdMap.remove(url);
                        }

                    }

                    @Override
                    public void onProgress(long bytesRead, long contentLength, boolean done) {

                        if (globalCallBack != null) {
                            globalCallBack.onProgress(url, bytesRead, contentLength, done);
                        }

                        if (downMap.containsKey(url)) {

                            CanFileGlobalCallBack callBack = downMap.get(url);

                            if (callBack != null) {
                                callBack.onProgress(url, bytesRead, contentLength, done);
                            }
                        }

                        if (request.isNotificationVisibility()) {
                            if (System.currentTimeMillis() - showTime > 500) {

                                int progress = (int) (bytesRead / (double) contentLength * 100);


                                showDownProgressNotify(progress, context, finalFileName, request);
                                showTime = System.currentTimeMillis();

                            }
                        }

                    }
                }, fileName);


    }

    /**
     * 取消通知
     */
    private void hideDownNotify(Context context, Request request) {


        if (notificationMrg == null) {
            notificationMrg = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        notificationMrg.cancel(getNotifyId(request));
    }

    /**
     * 下载进度显示
     */
    private void showDownProgressNotify(int progress, Context context, String fileName, Request request) {

        NotificationCompat.Builder builder = getNotifyBuilderProgress(context, fileName,
                context.getString(R.string.can_downing), progress + "%", null,
                R.mipmap.ic_launcher, null, 100, progress,
                false,
                null, Notification.DEFAULT_LIGHTS, true, false);


        if (notificationMrg == null) {
            notificationMrg = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
        }


        notificationMrg.notify(getNotifyId(request), builder.build());
    }

    /**
     * 下载完成
     */
    private void showDownSuccessNotify(Context context, String fileName, String filePath, Request request) {


        String contentText = context.getString(R.string.can_down_open);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), DownFileUtils.getMimeType(filePath));

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(fileName)
                .setContentText(contentText).setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_ALL);


        if (notificationMrg == null) {
            notificationMrg = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        notificationMrg.notify(getNotifyId(request), builder.build());
    }


    /**
     * 下载失败
     */
    private void showDownFailNotify(Context context, String fileName, Request request) {


        String contentText = context.getString(R.string.can_down_fail);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(), 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(fileName)
                .setContentText(contentText).setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL);


        if (notificationMrg == null) {
            notificationMrg = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        notificationMrg.notify(getNotifyId(request), builder.build());
    }

    private int getNotifyId(Request request) {
        String key = request.url;
        int id = 1000;
        if (downIdMap.containsKey(key)) {
            id = downIdMap.get(key);
        }
        return id;
    }

    /**
     * 设置通知栏
     *
     * @return NotificationCompat.Builder
     */
    private NotificationCompat.Builder getNotifyBuilderProgress(Context context, String contentTitle,
                                                                String contentText, String contentInfo, Bitmap largeIcon,
                                                                int smallIcon, Class contentclass, int max, int progress,
                                                                boolean indeterminate,
                                                                PendingIntent deleteIntent, int defaults, boolean autoCancel, boolean onGo) {
        NotificationCompat.Builder builder = getNotifyBuilder(context, contentTitle, contentText,
                contentInfo, largeIcon, smallIcon, contentclass, deleteIntent, defaults, autoCancel, onGo);

        builder.setProgress(max, progress, indeterminate);


        return builder;

    }

    /**
     * 得到一个通知
     *
     * @return NotificationCompat.Builder
     */
    private NotificationCompat.Builder getNotifyBuilder(Context context, String contentTitle, String contentText,
                                                        String contentInfo, Bitmap largeIcon, int smallIcon,
                                                        Class contentclass,
                                                        PendingIntent deleteIntent, int defaults, boolean autoCancel, boolean onGo) {


        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(), 0);


        return new NotificationCompat.Builder(context)
                .setLargeIcon(largeIcon).setSmallIcon(smallIcon)
                .setContentInfo(contentInfo).setContentTitle(contentTitle)
                .setContentText(contentText).setAutoCancel(autoCancel)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(onGo)
                .setContentIntent(pendingIntent).setDeleteIntent(deleteIntent)
                .setDefaults(defaults);


    }

    private String secureHashKey(final String key) {

        try {
            return SecureHashUtil.makeSHA1HashBase64(key.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return key;
    }


    public static class Request {

        private String url;

        private boolean isNotificationVisibility = true;

        private Map<String, String> requestHeader = new ArrayMap<>();

        public Request(String url) {
            this.url = url;
        }


        public String getUrl() {
            return url;
        }

        public Request addRequestHeader(String header, String value) {
            requestHeader.put(header, value);
            return this;
        }

        public Map<String, String> getRequestHeader() {
            return requestHeader;
        }

        public Request setNotificationVisibility(boolean visibility) {
            isNotificationVisibility = visibility;

            return this;
        }

        public boolean isNotificationVisibility() {
            return isNotificationVisibility;
        }
    }


}
