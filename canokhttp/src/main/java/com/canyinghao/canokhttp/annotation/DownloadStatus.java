package com.canyinghao.canokhttp.annotation;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 下载状态
 * @author canyinghao
 */
@IntDef({DownloadStatus.INIT,DownloadStatus.DOWNLOADING,DownloadStatus.PAUSE,DownloadStatus.COMPLETED})
@Retention(RetentionPolicy.SOURCE)
public @interface DownloadStatus {

    int  INIT = 1;//初始化状态
    int DOWNLOADING = 2;//正在下载状态
    int PAUSE = 3;//暂停状态
    int COMPLETED = 4;//下载完成状态

}