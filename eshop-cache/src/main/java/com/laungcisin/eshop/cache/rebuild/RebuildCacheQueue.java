package com.laungcisin.eshop.cache.rebuild;

import com.laungcisin.eshop.cache.model.ProductInfo;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * 重建缓存的队列
 */
public class RebuildCacheQueue {
    private ArrayBlockingQueue<ProductInfo> queue = new ArrayBlockingQueue<>(1000);

    public void addProductInfo(ProductInfo productInfo) {
        try {
            queue.put(productInfo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ProductInfo takeProductInfo() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private RebuildCacheQueue() {
    }

    private static class Singleton {
        private static RebuildCacheQueue instance;

        static {
            instance = new RebuildCacheQueue();
        }

        private static RebuildCacheQueue getInstance() {
            return instance;
        }
    }

    public static RebuildCacheQueue getInstance() {
        return Singleton.getInstance();
    }

    public static void init() {
        getInstance();
    }
}
