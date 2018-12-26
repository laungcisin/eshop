package com.laungcisin.eshop.inventory.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class RequestQueue {
    /**
     * 内存队列
     */
    private List<ArrayBlockingQueue<Request>> queues = new ArrayList<>();
    private Map<Integer, Boolean> flatMap = new ConcurrentHashMap<>();

    private RequestQueue() {
    }

    private static class Singleton {
        private static RequestQueue instance;

        static {
            instance = new RequestQueue();
        }

        private static RequestQueue getInstance() {
            return instance;
        }
    }

    public static RequestQueue getInstance() {
        return Singleton.getInstance();
    }

    /**
     * 添加一个内存队列
     *
     * @param queue
     */
    public void addQueue(ArrayBlockingQueue<Request> queue) {
        queues.add(queue);
    }

    /**
     * 获取内存队列的数量
     *
     * @return
     */
    public int queueSize() {
        return queues.size();
    }

    /**
     * 获取内存队列
     * @param index
     * @return
     */
    public ArrayBlockingQueue<Request> getQueue(int index) {
        return queues.get(index);
    }

    public Map<Integer, Boolean> getFlatMap() {
        return flatMap;
    }
}
