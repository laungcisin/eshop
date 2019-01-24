package com.laungcisin.eshop.storm.service;

import com.laungcisin.eshop.storm.model.ProductInfo;
import com.laungcisin.eshop.storm.model.ShopInfo;

public interface CacheService {
    /**
     * 从将商品信息保存到本地缓存中
     *
     * @param productInfo
     * @return
     */
    ProductInfo saveLocalCache(ProductInfo productInfo);

    /**
     * 从本地缓存中获取商品信息
     *
     * @param id
     * @return
     */
    ProductInfo getLocalCache(Long id);

    ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo);

    ProductInfo getProductInfoFromLocalCache(Long productId);

    void saveProductInfo2RedisCache(ProductInfo productInfo);

    ProductInfo getProductInfoFromRedisCache(Long productId);

    ShopInfo saveShopInfo2LocalCache(ShopInfo shopInfo);

    ShopInfo getShopInfoFromLocalCache(Long shopId);

    void saveShopInfo2RedisCache(ShopInfo shopInfo);

    ShopInfo getShopInfoFromRedisCache(Long shopId);
}
