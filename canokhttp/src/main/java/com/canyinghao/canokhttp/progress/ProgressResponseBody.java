package com.canyinghao.canokhttp.progress;

import android.text.TextUtils;

import com.canyinghao.canokhttp.CanOkHttp;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.MediaType;
import okhttp3.Response;
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

public class ProgressResponseBody extends ResponseBody {

    private final ResponseBody responseBody;

    private BufferedSource bufferedSource;


    private CanOkHttp okHttp;


    private long firstLength;
    private long totalLength;

    public ProgressResponseBody(@NonNull Response originalResponse, @NonNull CanOkHttp okHttp) {

        this.responseBody = originalResponse.body();
        this.okHttp = okHttp;
        try {
            String length = originalResponse.header("Content-Length");
            String range = originalResponse.header("Content-Range");
            if (!TextUtils.isEmpty(length) && !TextUtils.isEmpty(range)) {
                firstLength = okHttp.getCompletedSize();
                totalLength = firstLength + contentLength();
            } else {
                totalLength = contentLength();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            totalLength = contentLength();
        }

    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
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
                if (totalBytesRead == 0) {
                    totalBytesRead = firstLength;
                }
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;

                okHttp.sendProgressMsg(totalBytesRead, totalLength, bytesRead == -1);

                return bytesRead;
            }
        };
    }
}
