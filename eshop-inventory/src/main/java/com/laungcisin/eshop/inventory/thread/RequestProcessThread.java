package com.laungcisin.eshop.inventory.thread;

import com.laungcisin.eshop.inventory.request.ProductInventoryCacheRefreshRequest;
import com.laungcisin.eshop.inventory.request.ProductInventoryDBUpdateRequest;
import com.laungcisin.eshop.inventory.request.Request;
import com.laungcisin.eshop.inventory.request.RequestQueue;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

/**
 * 执行请求的工作线程
 */
public class RequestProcessThread implements Callable<Boolean> {
    /**
     * 自己监控的内存队列
     */
    private ArrayBlockingQueue<Request> queue;

    public RequestProcessThread(ArrayBlockingQueue<Request> queue) {
        this.queue = queue;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            while (true) {
                Request request = queue.take();

                System.out.println("----------------------------: 从队列中获取请求， 商品id：" + request.getProductId());

                boolean forceRefresh = request.isForceRefresh();
                // 做去重，不强制刷新缓存
                if (!forceRefresh) {
                    // 读请求过滤
                    Integer productId = request.getProductId();
                    RequestQueue requestQueue = RequestQueue.getInstance();
                    Map<Integer, Boolean> flagMap = requestQueue.getFlatMap();

                    // 如果是更新操作
                    if (request instanceof ProductInventoryDBUpdateRequest) {
                        flagMap.put(productId, true);
                    } else if (request instanceof ProductInventoryCacheRefreshRequest) {// 如果是读操作
                        Boolean flag = flagMap.get(productId);

                        if (flag == null) {
                            flagMap.put(productId, false);
                        }

                        // 说明之前已经有一个更新操作，此时将操作标识改成false，表明有一个读操作
                        if (flag != null && flag) {
                            flagMap.put(productId, false);
                        }

                        // 说明之前已经有一个更新操作和读操作，此时过滤此读操作
                        if (flag != null && !flag) {
                            return true;
                        }
                    }
                }

                // 执行process方法
                request.process();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Boolean.TRUE;
    }
}
