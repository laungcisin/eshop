package com.laungcisin.eshop.inventory.request;

/**
 * 请求接口
 */
public interface Request {
    void process();

    Integer getProductId();

    boolean isForceRefresh();
}
