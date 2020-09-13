package com.taike.rtspplayer.rtsp;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
    private static final String TAG = "ThreadPool";
    private ExecutorService executorService;

    private ThreadPool() {
        if (executorService != null) {
            return;
        }
        Log.d(TAG, "ThreadPool() called");
        executorService = Executors.newCachedThreadPool();
    }

    private final static ThreadPool instance = new ThreadPool();

    public static ThreadPool getInstance() {
        return instance;
    }

    public void submit(Runnable runnable) {
        if (!executorService.isShutdown()) {
            executorService.submit(runnable);
        }
    }

    public void shutDown() {
        executorService.shutdown();
    }

    public boolean isShtDown() {
        return executorService.isShutdown();
    }

    public static int getNumAvailableCores() {
        return Runtime.getRuntime().availableProcessors();
    }

}
