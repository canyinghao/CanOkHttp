package com.canyinghao.canokhttp.threadpool;


import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ThreadPool {

    private static int defaultSchedule;

    private static ThreadPool instance;

    public static void init(int defaultSchedule) {
        ThreadPool.defaultSchedule = defaultSchedule;
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

    public <T, O> void single(O o, final Job<T> job, final FutureListener<T> listener, Scheduler schedule, Scheduler observe) {

        Single.just(o)
                .map(new Function<O, T>() {

                    @Override
                    public T apply(O o) throws Exception {

                        return job.run();
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


    }


    public <T, O> void single(O o, Job<T> job) {


        this.single(o, job, null);
    }


    public <T, O> void single(O o, Job<T> job, final FutureListener<T> listener) {
        Scheduler scheduler = null;
        switch (defaultSchedule) {


            case 1:
                scheduler = Schedulers.computation();
                break;

            case 2:
                scheduler = Schedulers.io();
                break;

            case 3:
                scheduler = Schedulers.trampoline();
                break;

            case 4:
                scheduler = Schedulers.newThread();
                break;

            default:
                scheduler = Schedulers.single();
                break;
        }

        this.single(o, job, listener, scheduler, AndroidSchedulers.mainThread());
    }


    public <T, O> void single(O o, Job<T> job, final FutureListener<T> listener, Scheduler schedule) {


        this.single(o, job, listener, schedule, AndroidSchedulers.mainThread());
    }


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
        Scheduler scheduler = null;
        switch (defaultSchedule) {


            case 1:
                scheduler = Schedulers.computation();
                break;

            case 2:
                scheduler = Schedulers.io();
                break;

            case 3:
                scheduler = Schedulers.trampoline();
                break;

            case 4:
                scheduler = Schedulers.newThread();
                break;

            default:
                scheduler = Schedulers.single();
                break;
        }

        this.submit(job, listener, scheduler, AndroidSchedulers.mainThread());
    }


    public <T> void submit(Job<T> job, final FutureListener<T> listener, Scheduler schedule) {


        this.submit(job, listener, schedule, AndroidSchedulers.mainThread());
    }
}
