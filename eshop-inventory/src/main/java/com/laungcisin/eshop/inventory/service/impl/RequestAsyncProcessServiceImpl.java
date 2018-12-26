package com.laungcisin.eshop.inventory.service.impl;

import com.laungcisin.eshop.inventory.request.Request;
import com.laungcisin.eshop.inventory.request.RequestQueue;
import com.laungcisin.eshop.inventory.service.RequestAsyncProcessService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;


/**
 * 请求异步处理Service实现类
 */
@Service("requestAsyncProcessService")
public class RequestAsyncProcessServiceImpl implements RequestAsyncProcessService {

    @Override
    public void process(Request request) {
        System.out.println("----------------------------: 请求路由， 商品id：" + request.getProductId());
        // 做请求的路由,根据每个请求的商品id,路由到对应的内存队列中去
        ArrayBlockingQueue<Request> routingQueue = getRoutingQueue(request.getProductId());

        // 将请求放入到对应的队列中,完成路由操作
        try {
            routingQueue.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ArrayBlockingQueue<Request> getRoutingQueue(Integer productId) {
        RequestQueue requestQueue = RequestQueue.getInstance();

        // 先获取productId的hash值
        String key = String.valueOf(productId);
        int h;
        int hash = (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);

        // 对hash值取模，将hash值路由到指定的内存队列中，比如内存队列大小8
        // 用内存队列的数量对hash值取模之后，结果一定是在0~7之间
        // 所以任何一个商品id都会被固定路由到同样的一个内存队列中去的
        int index = (requestQueue.queueSize() - 1) & hash;

        System.out.println("----------------------------: 路由内存队列，商品id=" + productId + ", 队列索引=" + index);
        return requestQueue.getQueue(index);
    }
}
