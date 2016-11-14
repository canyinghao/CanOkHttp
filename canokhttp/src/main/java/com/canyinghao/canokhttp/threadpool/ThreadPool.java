package com.canyinghao.canokhttp.threadpool;


import android.app.Activity;
import android.text.TextUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {


    private static ThreadPool instance;


    private int maxRequests = 64;

    private final ExecutorService mExecutor;


    private final Deque<Worker> readyAsyncCalls = new ArrayDeque<>();


    private final Deque<Worker> runningAsyncCalls = new ArrayDeque<>();

    private Runnable idleCallback;

    private String mTag;


    public static ThreadPool getInstance() {

        if (instance == null) {
            synchronized (ThreadPool.class) {
                if (instance == null) {
                    instance = new ThreadPool();
                }
            }
        }

        return instance;
    }


    private ThreadPool() {

        this.mExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new PriorityThreadFactory("thread-pool", 10));

    }

    public synchronized void setIdleCallback(Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }


    public synchronized <T> Future<T> submit(Job<T> job, FutureListener<T> listener) {
        Worker<T> w = new Worker<T>(this, job, listener);
        w.setTag(mTag);
        mTag = null;

        if (!TextUtils.isEmpty(w.getTag())) {
            WorkerManager.putCall(w.getTag(), w);
        }

        if (runningAsyncCalls.size() < maxRequests) {
            runningAsyncCalls.add(w);
            mExecutor.execute(w);
        } else {
            readyAsyncCalls.add(w);
        }

        return w;
    }


    public synchronized <T> Future<T> submit(Job<T> job) {


        return this.submit(job, null);
    }


    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public ThreadPool setTag(Object object) {
        if (object instanceof Activity) {
            Activity activity = (Activity) object;
            this.mTag = activity.getClass().getCanonicalName();
        } else if (object instanceof android.support.v4.app.Fragment) {
            android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) object;
            this.mTag = fragment.getActivity().getClass().getCanonicalName();
        } else if (object instanceof android.app.Fragment) {
            android.app.Fragment fragment = (android.app.Fragment) object;
            this.mTag = fragment.getActivity().getClass().getCanonicalName();
        } else if (object != null) {
            this.mTag = object.toString();
        }
        return this;
    }


    public String getTag() {
        return mTag;
    }

    public void finished(Worker worker) {

        if (!TextUtils.isEmpty(worker.getTag())) {
            WorkerManager.removeCall(worker.getTag(), worker);
        }

        finished(runningAsyncCalls, worker, true);

    }

    private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {
        int runningCallsCount;
        Runnable idleCallback;
        synchronized (this) {
            if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
            if (promoteCalls) promoteCalls();
            runningCallsCount = runningCallsCount();
            idleCallback = this.idleCallback;
        }

        if (runningCallsCount == 0 && idleCallback != null) {
            idleCallback.run();
        }
    }


    public synchronized int runningCallsCount() {
        return runningAsyncCalls.size();
    }


    private void promoteCalls() {
        if (runningAsyncCalls.size() >= maxRequests) return; // Already running max capacity.
        if (readyAsyncCalls.isEmpty()) return; // No ready calls to promote.

        for (Iterator<Worker> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            Worker worker = i.next();
            i.remove();
            runningAsyncCalls.add(worker);
            this.mExecutor.execute(worker);


            if (runningAsyncCalls.size() >= maxRequests) return; // Reached max capacity.
        }
    }


}
