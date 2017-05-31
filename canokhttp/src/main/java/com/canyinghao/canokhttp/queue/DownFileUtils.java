package com.canyinghao.canokhttp.queue;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.Locale;

/**
 * Created by jianyang on 2017/5/31.
 */

public class DownFileUtils {




    private static String getSuffix(File file) {
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

    public static String getMimeType(String filePath){
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
}
