package com.taike.rtspplayer.rtsp;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerThreadPool {
    private static final String TAG = "ThreadPool";
    private ExecutorService executorService;

    private PlayerThreadPool() {
        if (executorService != null) {
            return;
        }
        Log.d(TAG, "ThreadPool() called");
        executorService = Executors.newCachedThreadPool();
    }

    private final static PlayerThreadPool instance = new PlayerThreadPool();

    public static PlayerThreadPool getInstance() {
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
