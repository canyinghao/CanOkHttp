package com.canyinghao.canokhttp.queue;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.widget.RemoteViews;

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

import androidx.collection.ArrayMap;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

/**
 * DownloadManager
 * Created by canyinghao on 2017/5/31.
 */

public class DownloadManager {

    public static final String PUSH_CHANNEL_ID = "PUSH_NOTIFY_ID";
    public static final String PUSH_CHANNEL_NAME = "PUSH_NOTIFY_NAME";
    public static final String PUSH_CHANNEL_CLICK = "com.ACTION_NOTIFICATION_CLICK";

    private Context context;
    private NotificationManager notificationMrg;

    private Map<String, CanFileGlobalCallBack> downMap = new ArrayMap<>();
    private Map<String, Long> downIdMap = new ArrayMap<>();

    private CanFileGlobalCallBack globalCallBack;
    private static  DownloadManager instance;
    private CanOkHttp canOkHttp;
    private NotificationBroadcast receiver;
    private int count;

    public static DownloadManager getInstance(Context context) {
        if(instance==null){
            instance = new DownloadManager(context);
        }
        return instance;
    }

    private DownloadManager(Context context) {
        this.context = context;

        try {
            receiver = new NotificationBroadcast();
            IntentFilter filter = new IntentFilter();
            filter.addAction(PUSH_CHANNEL_CLICK);
            context.registerReceiver(receiver, filter);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public void cancelDownLoad(){
        if(canOkHttp!=null){
            canOkHttp.setDownloadStatus(DownloadStatus.PAUSE);
        }
    }

    public void setGlobalCallBack(CanFileGlobalCallBack globalCallBack) {
        this.globalCallBack = globalCallBack;
    }

    public CanFileGlobalCallBack getGlobalCallBack() {
        return globalCallBack;
    }

    public void enqueue(final Request request, CanFileGlobalCallBack canFileCallBack) {
        final String url = request.url;
        final String downPath = request.downPath;

        if (canFileCallBack != null) {

            downMap.put(url, canFileCallBack);
        }


        if (downIdMap.containsKey(url)) {

            if (request.isGlobal()&&globalCallBack != null) {
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


        if(request.isSaveXml()){
            String filePath = CanPreferenceUtil.getString(secureHashKey(url), "", context);

            File file = new File(filePath);

            if (file.isFile() && file.exists()) {

                if (request.isGlobal()&&globalCallBack != null) {
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
        }


        String fileName = request.getFileName();

        if(TextUtils.isEmpty(fileName)){
            fileName = System.currentTimeMillis() + "";
            String tempUrl = url;
            if(url.contains("?")){
                String[] splits =  url.split("\\?");
                if(splits.length>0){
                    tempUrl = splits[0];
                }
            }

            String[] splits = tempUrl.split("/");
            if (splits.length > 0) {
                String str = splits[splits.length - 1];

                if (!TextUtils.isEmpty(str)) {
                    fileName = str;
                }
            }
        }


        downIdMap.put(url, System.currentTimeMillis());

        final String finalFileName = fileName;


        if(canOkHttp==null){
            canOkHttp = CanOkHttp.getInstance();
        }
        if (request.getRequestHeader() != null && !request.getRequestHeader().isEmpty()) {

            Set<String> set = request.getRequestHeader().keySet();

            for (String key : set) {

                canOkHttp.addHeader(key, request.getRequestHeader().get(key));
            }
        }
        if(!TextUtils.isEmpty(downPath)){
            canOkHttp.setDownloadFileDir(downPath);
        }
        canOkHttp.setDownCoverFile(true)
                .setTag(url)
                .startDownload(request.url, new CanFileCallBack() {

                    private long showTime;

                    @Override
                    public void onFailure(@ResultType int type, int code, String e) {


                        try {
                            if (request.isGlobal()&&globalCallBack != null) {
                                globalCallBack.onFailure(url, type, code, e);
                            }
                        } catch (Throwable exception) {
                            exception.printStackTrace();
                        }


                        try {
                            if (downMap.containsKey(url)) {

                                CanFileGlobalCallBack callBack = downMap.get(url);

                                if (callBack != null) {
                                    callBack.onFailure(url, type, code, e);
                                }
                                downMap.remove(url);
                            }
                        } catch (Throwable exception) {
                            exception.printStackTrace();
                        }

                        try {
                            if (request.isNotificationVisibility()) {
                                showDownFailNotify(context, finalFileName, request);
                            }
                        } catch (Throwable exception) {
                            exception.printStackTrace();
                        }

                        try {
                            if (downIdMap.containsKey(url)) {
                                downIdMap.remove(url);
                            }
                        } catch (Throwable exception) {
                            exception.printStackTrace();
                        }

                    }

                    @Override
                    public void onFileSuccess(@DownloadStatus int status, String msg, String filePath) {

                        try {
                            if (request.isGlobal()&&globalCallBack != null) {
                                globalCallBack.onFileSuccess(url, status, msg, filePath);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        try {
                            if (downMap.containsKey(url)) {

                                CanFileGlobalCallBack callBack = downMap.get(url);

                                if (callBack != null) {
                                    callBack.onFileSuccess(url, status, msg, filePath);
                                }
                                downMap.remove(url);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }


                        try{
                            if(request.isSaveXml()){
                                CanPreferenceUtil.putString(secureHashKey(url), filePath, context);
                            }

                            if (request.isNotificationVisibility()) {
                                File file = new File(filePath);
                                if (file.isFile() && file.exists()) {
                                    showDownSuccessNotify(context, finalFileName, filePath, request);
                                } else {
                                    hideDownNotify(context, request);
                                }
                            }
                        }catch (Throwable e){
                            e.printStackTrace();
                        }


                        try {
                            if (downIdMap.containsKey(url)) {
                                downIdMap.remove(url);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onProgress(long bytesRead, long contentLength, boolean done) {

                        try {
                            if (request.isGlobal()&&globalCallBack != null) {
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
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                    }
                }, fileName);


    }

    public void removeKey(String key){
        try {
            downIdMap.remove(key);
            downMap.remove(key);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消通知
     */
    private void hideDownNotify(Context context, Request request) {


        notificationMrg = getNotificationManager(context);

        notificationMrg.cancel(getNotifyId(request));
    }

    /**
     * 下载进度显示
     */
    private void showDownProgressNotify(int progress, Context context, String fileName, Request request) {

        NotificationCompat.Builder builder = getNotifyBuilderProgress(context, fileName,
                context.getString(R.string.can_downing), progress + "%", null,
                R.mipmap.icon, null, 100, progress,
                false,
                null, Notification.DEFAULT_LIGHTS, true, false);

        builder.setWhen(getNotifyTime(request));

        notificationMrg = getNotificationManager(context);


        notificationMrg.notify(getNotifyId(request), builder.build());
    }

    private NotificationManager getNotificationManager(Context context) {
        if (notificationMrg == null) {
            notificationMrg = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(PUSH_CHANNEL_ID, PUSH_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                if (notificationMrg != null) {
                    notificationMrg.createNotificationChannel(channel);
                }
            }
        }
        return notificationMrg;
    }

    /**
     * 下载完成
     */
    private void showDownSuccessNotify(Context context, String fileName, String filePath, Request request) {


        String contentText = context.getString(R.string.can_down_open);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(getFileUri(context,new File(filePath)), DownFileUtils.getMimeType(context,filePath));

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,PUSH_CHANNEL_ID)
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle(fileName)
                .setContentText(contentText).setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_ALL);


        notificationMrg = getNotificationManager(context);

        notificationMrg.notify(getNotifyId(request), builder.build());
    }


    /**
     * 下载失败
     */
    private void showDownFailNotify(Context context, String fileName, Request request) {


        String contentText = context.getString(R.string.can_down_fail);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(), 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,PUSH_CHANNEL_ID)
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle(fileName)
                .setContentText(contentText).setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL);


        notificationMrg = getNotificationManager(context);

        notificationMrg.notify(getNotifyId(request), builder.build());
    }

    private int getNotifyId(Request request) {
        String key = request.url;
        int id = 10000;
        if (downIdMap.containsKey(key)) {
           long time = downIdMap.get(key);
            id = (int) (time%10000);
        }

        return id;
    }


    private long getNotifyTime(Request request) {
        String key = request.url;
        long id = System.currentTimeMillis();
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

        RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.layout_download_nitification);
        views.setProgressBar(R.id.progress,max,progress,indeterminate);
        views.setTextViewText(R.id.tv_des,contentTitle+"  "+contentText+"  "+contentInfo);
        PendingIntent cancelIntent = PendingIntent.getBroadcast(context,1,new Intent(PUSH_CHANNEL_CLICK),PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.btn_cancel,cancelIntent);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(), 0);


        return new NotificationCompat.Builder(context,PUSH_CHANNEL_ID)
                .setContent(views)
                .setLargeIcon(largeIcon).setSmallIcon(smallIcon)
                .setContentInfo(contentInfo).setContentTitle(contentTitle)
                .setContentText(contentText).setAutoCancel(autoCancel)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(onGo)
                .setContentIntent(pendingIntent).setDeleteIntent(deleteIntent)
                .setDefaults(defaults);



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


        return new NotificationCompat.Builder(context,PUSH_CHANNEL_ID)
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

        private String downPath;

        private String fileName;

        private boolean isNotificationVisibility = true;

        private boolean isGlobal = true;

        private boolean isSaveXml = true;

        private Map<String, String> requestHeader = new ArrayMap<>();

        public Request(String url,String downPath) {
            this.url = url;
            this.downPath = downPath;
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

        public void setGlobal(boolean global) {
            isGlobal = global;
        }

        public boolean isGlobal() {
            return isGlobal;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }

        public boolean isSaveXml() {
            return isSaveXml;
        }

        public void setSaveXml(boolean saveXml) {
            isSaveXml = saveXml;
        }
    }



    public Uri getFileUri(Context context,File file) {

        if (Build.VERSION.SDK_INT >= 24) {

            return FileProvider.getUriForFile(context.getApplicationContext(), context.getApplicationContext().getPackageName() + ".fileprovider", file);

        } else {
            return Uri.fromFile(file);
        }

    }
}
