package com.canyinghao.canokhttp.threadpool;

import android.util.Log;


public class Worker<T> implements Runnable, Future<T>, JobContext {
    private static final String TAG = "Worker";
    private ThreadPool threadPool;
    private Job<T> mJob;
    private FutureListener<T> mListener;

    private volatile boolean mIsCanceled;
    private boolean mIsDone;
    private T mResult;

    private String mTag;


    public Worker(ThreadPool threadPool, Job<T> job, FutureListener<T> listener) {
        this.threadPool = threadPool;
        this.mJob = job;
        this.mListener = listener;
    }

    public void run() {
        T result = null;

        if (!mIsCanceled) {
            try {
                result = this.mJob.run(this);
            } catch (Throwable var5) {
                Log.w(TAG, "Exception in running a job", var5);
            }
        }

        synchronized (this) {
            this.mResult = result;
            this.mIsDone = true;

            this.notifyAll();

            threadPool.finished(this);
        }

        if (!mIsCanceled && this.mListener != null) {
            this.mListener.onFutureDone(this);
        }

    }


    public synchronized void cancel() {
        if (!this.mIsCanceled) {
            this.mIsCanceled = true;

        }
    }


    public void setTag(String mTag) {
        this.mTag = mTag;
    }

    public String getTag() {
        return mTag;
    }

    public boolean isCanceled() {
        return this.mIsCanceled;
    }

    public synchronized boolean isDone() {
        return this.mIsDone;
    }

    public synchronized T get() {
        while (!this.mIsDone) {
            try {
                this.wait();
            } catch (Exception var2) {
                Log.w(TAG, "ingore exception", var2);
            }
        }

        return this.mResult;
    }

    public void waitDone() {
        this.get();
    }


}