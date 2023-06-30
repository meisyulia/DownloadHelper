package com.example.downloadhelper.util.down;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {
    private static ThreadPool threadPool;
    //可同时下载数
    private int CORE_POOL_SIZE = 3;
    //缓存队列的大小
    private int MAX_POOL_SIZE = 20;
    //超时时间（秒）,如果超时则会被回收
    private long KEEP_ALIVE = 10L;

    public static ThreadPool getInstance(){
        if (threadPool == null) {
            synchronized (ThreadPool.class){
                threadPool = new ThreadPool();
            }
        }
        return threadPool;
    }

    private ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger();
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable,"download_thread--"+mCount.getAndIncrement());
        }
    };
    private ThreadPoolExecutor threadPoolExecutor;

    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize == 0) {
            return;
        }
        CORE_POOL_SIZE = corePoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        if (maxPoolSize == 0) {
            return;
        }
        MAX_POOL_SIZE = maxPoolSize;
    }

    public int getCorePoolSize() {
        return CORE_POOL_SIZE;
    }

    public int getMaxPoolSize() {
        return MAX_POOL_SIZE;
    }

    public ThreadPoolExecutor getThreadPoolExecutor(){
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
                    new LinkedBlockingDeque<Runnable>(), sThreadFactory);
        }
        return threadPoolExecutor;
    }
}
