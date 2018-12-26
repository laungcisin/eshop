package com.laungcisin.eshop.inventory.service;

import com.laungcisin.eshop.inventory.request.Request;

/**
 * 请求异步执行Service
 */
public interface RequestAsyncProcessService {
    void process(Request request);

}
