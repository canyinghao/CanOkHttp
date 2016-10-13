package com.canyinghao.canokhttp.util;

import android.text.TextUtils;

import java.security.MessageDigest;

import okhttp3.MediaType;


public class CanOkHttpUtil {

    /**
     * MD5加密：生成16位密文
     * @param originString 加密字符串
     * @param isUpperCase 是否生成大写密文
     * @return String
     * @throws Exception
     */
    public static String MD5StringTo16Bit(String originString,boolean isUpperCase) throws Exception{
        String result = MD5StringTo32Bit(originString,isUpperCase);
        if(result.length() == 32){
            return result.substring(8,24);
        }
        return "";
    }

    /**
     * MD5加密：生成32位密文
     * @param originString 加密字符串
     * @param isUpperCase 是否生成大写密文
     * @return String
     * @throws Exception
     */
    public static String MD5StringTo32Bit(String originString,boolean isUpperCase) throws Exception{
        String result = "";
        if (originString != null) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte bytes[] = md.digest(originString.getBytes());
            for (int i = 0; i < bytes.length; i++) {
                String str = Integer.toHexString(bytes[i] & 0xFF);
                if (str.length() == 1) {
                    str += "F";
                }
                result += str;
            }
        }
        if(isUpperCase){
            return result.toUpperCase();
        }else{
            return result.toLowerCase();
        }
    }


    /**
     * 类型
     * @param url url
     * @return MediaType
     */
    public static MediaType fetchFileMediaType(String url){
        if(!TextUtils.isEmpty(url) && url.contains(".")){
            String extension = url.substring(url.lastIndexOf(".") + 1);
            if("png".equals(extension)){
                extension = "image/png";
            }else if("jpg".equals(extension)){
                extension = "image/jpg";
            }else if("jpeg".equals(extension)){
                extension = "image/jpeg";
            }else if("gif".equals(extension)){
                extension = "image/gif";
            }else if("bmp".equals(extension)){
                extension = "image/bmp";
            }else if("tiff".equals(extension)){
                extension = "image/tiff";
            }else if("ico".equals(extension)){
                extension = "image/ico";
            }else{
                return null;
            }
            return MediaType.parse(extension);
        }
        return null;
    }

}
