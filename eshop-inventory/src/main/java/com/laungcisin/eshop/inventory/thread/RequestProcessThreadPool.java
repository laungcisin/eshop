package com.laungcisin.eshop.inventory.thread;

import com.laungcisin.eshop.inventory.request.Request;
import com.laungcisin.eshop.inventory.request.RequestQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 单例实例:使用静态内部类形式
 */
public class RequestProcessThreadPool {
    // TODO：线程大小可配置

    private Integer threadPoolSize = 16;

    /**
     * 线程池
     */
    private ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);

    private RequestProcessThreadPool() {
        // 内存队列
        RequestQueue requestQueue = RequestQueue.getInstance();
        for (int i = 0; i < threadPoolSize; i++) {
            ArrayBlockingQueue<Request> queue = new ArrayBlockingQueue<>(128);
            requestQueue.addQueue(queue);
            threadPool.submit(new RequestProcessThread(queue));
        }
    }

    private static class Singleton {
        private static RequestProcessThreadPool instance;

        static {
            instance = new RequestProcessThreadPool();
        }

        private static RequestProcessThreadPool getInstance() {
            return instance;
        }
    }

    private static RequestProcessThreadPool getInstance() {
        return Singleton.getInstance();
    }

    /**
     * 初始化方法
     */
    public static void init() {
        getInstance();
    }

}
