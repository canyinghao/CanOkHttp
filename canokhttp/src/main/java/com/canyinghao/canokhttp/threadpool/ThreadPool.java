package com.canyinghao.canokhttp.threadpool;


import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ThreadPool {


    private static ThreadPool instance;

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


    public synchronized <T> void submit(final Job<T> job, final FutureListener<T> listener,Scheduler schedule,Scheduler observe) {

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

                        try{
                            if (listener != null) {
                                listener.onFutureDone(value);
                            }
                        }catch (Throwable e){
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        try{
                            if (listener != null) {
                                listener.onFutureDone(null);
                            }
                        }catch (Throwable t){
                            t.printStackTrace();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }


    public synchronized <T> void submit(Job<T> job) {


        this.submit(job, null);
    }


    public synchronized <T> void submit(Job<T> job, final FutureListener<T> listener) {


        this.submit(job, listener,Schedulers.io(),AndroidSchedulers.mainThread());
    }


    public synchronized <T> void submit(Job<T> job, final FutureListener<T> listener,Scheduler schedule) {


        this.submit(job, listener,schedule,AndroidSchedulers.mainThread());
    }
}
