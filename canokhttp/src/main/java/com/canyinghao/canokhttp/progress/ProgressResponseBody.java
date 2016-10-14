package com.canyinghao.canokhttp.progress;

import android.support.annotation.NonNull;

import com.canyinghao.canokhttp.CanOkHttp;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 进度响应体
 *
 * @author canyinghao
 */

public  class ProgressResponseBody extends ResponseBody {

    private final ResponseBody responseBody;

    private BufferedSource bufferedSource;


    private CanOkHttp okHttp;

    public ProgressResponseBody(@NonNull ResponseBody responseBody, @NonNull  CanOkHttp okHttp) {
        this.responseBody = responseBody;
        this.okHttp = okHttp;

    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength()  {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source()  {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                if(totalBytesRead == 0) {
                    totalBytesRead = okHttp.getCompletedSize();
                }
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;

                okHttp.sendProgressMsg(totalBytesRead,contentLength(),bytesRead==-1);

                return bytesRead;
            }
        };
    }
}
