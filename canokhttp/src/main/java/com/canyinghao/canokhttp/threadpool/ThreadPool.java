package com.canyinghao.canokhttp.threadpool;

/**
 * Created by yangjian on 16/6/23.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {


    private static ThreadPool instance;


    private static final String TAG = "ThreadPool";
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 8;
    private static final int KEEP_ALIVE_TIME = 10;
    private static final int MODE_NONE = 0;
    private static final int MODE_CPU = 1;
    private static final int MODE_NETWORK = 2;

    private static int core_size = CORE_POOL_SIZE;
    private static int max_size = MAX_POOL_SIZE;

    public static final ThreadPool.JobContext JOB_CONTEXT_STUB = new ThreadPool.JobContextStub();
    private ThreadPool.ResourceCounter mCpuCounter;
    private ThreadPool.ResourceCounter mNetworkCounter;
    private final Executor mExecutor;

    public ThreadPool() {
        this(CORE_POOL_SIZE, MAX_POOL_SIZE);
    }

    public static ThreadPool getInstance() {

        if (instance == null) {
            instance = new ThreadPool(core_size, max_size);
        }

        return instance;
    }

    public static void init(int initPoolSize, int maxPoolSize) {
        core_size = initPoolSize;
        max_size = maxPoolSize;
    }

    public ThreadPool(int initPoolSize, int maxPoolSize) {
        this.mCpuCounter = new ThreadPool.ResourceCounter(MODE_NETWORK);
        this.mNetworkCounter = new ThreadPool.ResourceCounter(MODE_NETWORK);
        this.mExecutor = new ThreadPoolExecutor(initPoolSize, maxPoolSize, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("thread-pool", 10));
    }

    public <T> Future<T> submit(ThreadPool.Job<T> job, FutureListener<T> listener) {
        ThreadPool.Worker<T> w = new ThreadPool.Worker<T>(job, listener);
        this.mExecutor.execute(w);
        return w;
    }

    public <T> Future<T> submit(ThreadPool.Job<T> job) {
        return this.submit(job, null);
    }

    private class Worker<T> implements Runnable, Future<T>, ThreadPool.JobContext {
        private static final String TAG = "Worker";
        private ThreadPool.Job<T> mJob;
        private FutureListener<T> mListener;
        private ThreadPool.CancelListener mCancelListener;
        private ThreadPool.ResourceCounter mWaitOnResource;
        private volatile boolean mIsCancelled;
        private boolean mIsDone;
        private T mResult;
        private int mMode;

        public Worker(ThreadPool.Job<T> job, FutureListener<T> listener) {
            this.mJob = job;
            this.mListener = listener;
        }

        public void run() {
            T result = null;
            if (this.setMode(MODE_CPU)) {
                try {
                    result = this.mJob.run(this);
                } catch (Throwable var5) {
                    Log.w(TAG, "Exception in running a job", var5);
                }
            }

            synchronized (this) {
                this.setMode(MODE_NONE);
                this.mResult = result;
                this.mIsDone = true;
                this.notifyAll();
            }

            if (this.mListener != null) {
                this.mListener.onFutureDone(this);
            }

        }

        public synchronized void cancel() {
            if (!this.mIsCancelled) {
                this.mIsCancelled = true;
                if (this.mWaitOnResource != null) {
                    ThreadPool.ResourceCounter var1 = this.mWaitOnResource;
                    synchronized (this.mWaitOnResource) {
                        this.mWaitOnResource.notifyAll();
                    }
                }

                if (this.mCancelListener != null) {
                    this.mCancelListener.onCancel();
                }

            }
        }

        public boolean isCancelled() {
            return this.mIsCancelled;
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

        public synchronized void setCancelListener(ThreadPool.CancelListener listener) {
            this.mCancelListener = listener;
            if (this.mIsCancelled && this.mCancelListener != null) {
                this.mCancelListener.onCancel();
            }

        }

        public boolean setMode(int mode) {
            ThreadPool.ResourceCounter rc = this.modeToCounter(this.mMode);
            if (rc != null) {
                this.releaseResource(rc);
            }

            this.mMode = 0;
            rc = this.modeToCounter(mode);
            if (rc != null) {
                if (!this.acquireResource(rc)) {
                    return false;
                }

                this.mMode = mode;
            }

            return true;
        }

        private ThreadPool.ResourceCounter modeToCounter(int mode) {
            return mode == 1 ? ThreadPool.this.mCpuCounter : (mode == 2 ? ThreadPool.this.mNetworkCounter : null);
        }

        private boolean acquireResource(ThreadPool.ResourceCounter counter) {
            while (true) {
                synchronized (this) {
                    if (this.mIsCancelled) {
                        this.mWaitOnResource = null;
                        return false;
                    }

                    this.mWaitOnResource = counter;
                }

                synchronized (counter) {
                    if (counter.value <= 0) {
                        try {
                            counter.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    --counter.value;
                }

                synchronized (this) {
                    this.mWaitOnResource = null;
                    return true;
                }
            }
        }

        private void releaseResource(ThreadPool.ResourceCounter counter) {
            synchronized (counter) {
                ++counter.value;
                counter.notifyAll();
            }
        }
    }

    private static class ResourceCounter {
        public int value;

        public ResourceCounter(int v) {
            this.value = v;
        }
    }

    public interface CancelListener {
        void onCancel();
    }

    private static class JobContextStub implements ThreadPool.JobContext {
        private JobContextStub() {
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void setCancelListener(ThreadPool.CancelListener listener) {
        }

        @Override
        public boolean setMode(int mode) {
            return true;
        }
    }

    public interface JobContext {
        boolean isCancelled();

        void setCancelListener(ThreadPool.CancelListener var1);

        boolean setMode(int var1);
    }

    public interface Job<T> {
        T run(ThreadPool.JobContext var1);
    }
}
