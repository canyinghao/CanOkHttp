package com.canyinghao.canokhttp.downpic;

import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.handler.OkHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CanDownPicture {

    private static OkHttpClient client;
    private static int maxFail = 1;
    public static final String CONTENT_FILE_EXTENSION = ".cnt";
    public static final String TEMP_FILE_EXTENSION = ".tmp";


    public static void initCanDown(OkHttpClient okHttpClient ,int maxFailCount){
        client = okHttpClient;
        maxFail =maxFailCount;

    }

    public static void downPic(final String downPath, String url, Map<String, String> header, final OnDownPicListener downPicListener) {


        final PictureBean bean = getPicturePath(downPath, url);
        File f = new File(bean.path);
        if (f.exists() && f.length() > 0) {
            if (downPicListener != null) {
                downPicListener.onSuccess(bean.path, f.length());
            }
            return;
        }

        if (client == null) {
            client = CanOkHttp.getInstance().setOpenLog(true).getHttpClient();
        }

        Request.Builder builder = new Request.Builder();
        if (header != null && !header.isEmpty()) {
            for (String key : header.keySet()) {
                builder.addHeader(key, header.get(key));
            }
        }

        final Request request = builder.url(url).get().build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {

             int failCount;
            @Override
            public void onFailure(Call call, final IOException e) {

                if(failCount<maxFail){
                    failCount++;
                    call = client.newCall(request);
                    call.enqueue(this);
                }else{
                    if (downPicListener != null) {
                        OkHandler.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                downPicListener.onFail(e.getMessage());
                            }
                        });
                    }
                }




            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                String path = "";
                long size = 0;
                boolean isSuccess = false;
                String message = "";

                if (response != null) {
                    message = "Msg: " + response.message() + " Code: " + response.code();

                    if (response.isSuccessful()) {
                        size = response.body().contentLength();

                        path = bean.path;
                        isSuccess = writeToFile(response.body().byteStream(), bean);


                    }
                }


                if(!isSuccess&&failCount==0){
                    failCount++;
                    call = client.newCall(request);
                    call.enqueue(this);

                }else{
                    if (downPicListener != null) {


                        if (isSuccess) {
                            final String finalPath = path;
                            final long finalSize = size;
                            OkHandler.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    downPicListener.onSuccess(finalPath, finalSize);
                                }
                            });


                        } else {


                            final String finalMessage = message;
                            OkHandler.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    downPicListener.onFail(finalMessage);
                                }
                            });

                        }
                    }

                }




            }
        });


    }


    public static PictureBean getPicturePath(String mVersionDirectory, String url) {
        String key = secureHashKey(url);
        String dir = getSubdirectoryPath(mVersionDirectory, key);

        return new PictureBean(dir, key );
    }

    private static boolean writeToFile(InputStream byteStream, PictureBean bean) {


        return FileUtils.writeBinaryToFile(bean, byteStream);
    }


    private static String getSubdirectoryPath(String mVersionDirectory, String key) {
        String subdirectory = String.valueOf(Math.abs(key.hashCode() % 100));
        return mVersionDirectory + File.separator + subdirectory;
    }


    private static String secureHashKey(final String key) {

        try {
            return SecureHashUtil.makeSHA1HashBase64(key.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return key;
    }


}
