package com.canyinghao.canokhttp.threadpool;


import android.annotation.SuppressLint;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class ThreadPool {

    private static int defaultSchedule;

    private static ThreadPool instance;

    private static boolean isSingle;

    public static void initSet(int defaultSchedule,boolean isSingle) {
        ThreadPool.defaultSchedule = defaultSchedule;
        ThreadPool.isSingle = isSingle;

    }

    public static void initIoSchedulerHandler(){
        RxJavaPlugins.setInitIoSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
            @Override
            public Scheduler apply(@NonNull Callable<Scheduler> schedulerCallable) throws Exception {
                int processors =Runtime.getRuntime().availableProcessors();
                ThreadPoolExecutor executor =new ThreadPoolExecutor(processors * 2,
                        processors * 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(processors*10),new ThreadPoolExecutor.DiscardPolicy());

                return Schedulers.from(executor);
            }
        });
    }

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

    }
    @SuppressLint("CheckResult")
    public <T, O> void single(final O o, final SingleJob<O,T> job, final FutureListener<T> listener, Scheduler schedule, Scheduler observe) {
        if(isSingle){
            Single.just(o)
                    .map(new Function<O, T>() {

                        @Override
                        public T apply(O o) throws Exception {

                            return job.run(o);
                        }
                    }).observeOn(observe)
                    .subscribeOn(schedule)
                    .subscribeWith(new DisposableSingleObserver<T>() {
                        @Override
                        public void onSuccess(T t) {

                            try {
                                if (listener != null) {
                                    listener.onFutureDone(t);
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {

                            try {
                                if (listener != null) {
                                    listener.onFutureDone(null);
                                }
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    });
        }else{
            Observable.create(new ObservableOnSubscribe<T>() {
                @Override
                public void subscribe(ObservableEmitter<T> e) throws Exception {

                    T t = job.run(o);
                    e.onNext(t);
                    e.onComplete();

                }
            }).subscribeOn(schedule)
                    .observeOn(observe)
                    .subscribeWith(new DisposableObserver<T>() {
                        @Override
                        public void onNext(T value) {

                            try {
                                if (listener != null) {
                                    listener.onFutureDone(value);
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            try {
                                if (listener != null) {
                                    listener.onFutureDone(null);
                                }
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }



    }


    public <T, O> void single(O o, SingleJob<O,T> job) {


        this.single(o, job, null);
    }


    public <T, O> void single(O o, SingleJob<O,T> job, final FutureListener<T> listener) {
        Scheduler scheduler;
        switch (defaultSchedule) {


            case 1:
                scheduler = Schedulers.computation();
                break;

            case 3:
                scheduler = Schedulers.trampoline();
                break;

            case 4:
                scheduler = Schedulers.newThread();
                break;
            case 2:
            default:
                scheduler = Schedulers.io();
                break;
        }

        this.single(o, job, listener, scheduler, AndroidSchedulers.mainThread());
    }


    public <T, O> void single(O o, SingleJob<O,T> job, final FutureListener<T> listener, Scheduler schedule) {


        this.single(o, job, listener, schedule, AndroidSchedulers.mainThread());
    }


    @SuppressLint("CheckResult")
    public <T> void submit(final Job<T> job, final FutureListener<T> listener, Scheduler schedule, Scheduler observe) {

        Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> e) throws Exception {

                T t = job.run();
                e.onNext(t);
                e.onComplete();

            }
        }).subscribeOn(schedule)
                .observeOn(observe)
                .subscribeWith(new DisposableObserver<T>() {
                    @Override
                    public void onNext(T value) {

                        try {
                            if (listener != null) {
                                listener.onFutureDone(value);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        try {
                            if (listener != null) {
                                listener.onFutureDone(null);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }


    public <T> void submit(Job<T> job) {


        this.submit(job, null);
    }


    public <T> void submit(Job<T> job, final FutureListener<T> listener) {
        Scheduler scheduler;
        switch (defaultSchedule) {

            case 1:
                scheduler = Schedulers.computation();
                break;

            case 3:
                scheduler = Schedulers.trampoline();
                break;

            case 4:
                scheduler = Schedulers.newThread();
                break;
            case 5:
                scheduler = Schedulers.single();
                break;
            case 2:
            default:
                scheduler = Schedulers.io();
                break;
        }

        this.submit(job, listener, scheduler, AndroidSchedulers.mainThread());
    }


    public <T> void submit(Job<T> job, final FutureListener<T> listener, Scheduler schedule) {


        this.submit(job, listener, schedule, AndroidSchedulers.mainThread());
    }
}
