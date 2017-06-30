package com.canyinghao.canokhttp.progress;

import android.os.Message;
import android.support.annotation.NonNull;

import com.canyinghao.canokhttp.CanOkHttp;
import com.canyinghao.canokhttp.handler.OkHandler;
import com.canyinghao.canokhttp.handler.OkMessage;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 进度请求体
 *
 * @author canyinghao
 */
public class ProgressRequestBody extends RequestBody {

    private final RequestBody originalRequestBody;

    private BufferedSink bufferedSink;

    private CanOkHttp okHttp;

    public ProgressRequestBody(@NonNull RequestBody originalRequestBody, @NonNull CanOkHttp okHttp) {

        this.originalRequestBody = originalRequestBody;

        this.okHttp = okHttp;
    }

    @Override
    public MediaType contentType() {
        return originalRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return originalRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink originalSink) throws IOException {
        try{
            if (bufferedSink == null) {
                bufferedSink = Okio.buffer(sink(originalSink));
            }
            originalRequestBody.writeTo(bufferedSink);
            bufferedSink.flush();
        }catch (Throwable e){
            e.printStackTrace();
        }

    }


    private Sink sink(Sink originalSink) {
        return new ForwardingSink(originalSink) {
            long bytesWritten = 0L;
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;
                if (null != okHttp && okHttp.getCanCallBack() != null) {

                    //主线程回调

                    Message msg = new OkMessage(OkHandler.PROGRESS_CALLBACK,
                            okHttp.getCanCallBack(),
                            bytesWritten,
                            contentLength,
                            bytesWritten == contentLength)
                            .build();
                    OkHandler.getInstance().sendMessage(msg);
                }
            }
        };
    }


}
