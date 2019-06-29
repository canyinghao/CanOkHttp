package com.canyinghao.canokhttp.queue;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.Locale;

/**
 * Created by jianyang on 2017/5/31.
 */

public class DownFileUtils {


    public static String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return null;
        }
    }

    public static String getMimeType(Context context, String filePath) {
        boolean isApk = isApkFile(context, filePath);
        if (isApk) {
            return "application/vnd.android.package-archive";
        }
        File file = new File(filePath);
        String suffix = getSuffix(file);
        if (suffix == null) {
            return "file/*";
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (!TextUtils.isEmpty(type)) {
            return type;
        }
        return "file/*";
    }


    public static boolean isApkFile(Context context, String filePath) {

        String pkg = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo info = packageManager.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                pkg = appInfo.packageName;
            } else {
                pkg = "";
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return !TextUtils.isEmpty(pkg);
    }


}
