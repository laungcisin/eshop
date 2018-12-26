package com.laungcisin.eshop.cache.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.laungcisin.eshop.cache.model.ProductInfo;
import com.laungcisin.eshop.cache.model.ShopInfo;
import com.laungcisin.eshop.cache.service.CacheService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

@Service("cacheService")
public class CacheServiceImpl implements CacheService {
    private static final String CACHE_NAME = "local";

    @Resource
    private JedisCluster jedisCluster;

    @Override
    @CachePut(value = CACHE_NAME, key = "'key_'+#productInfo.getId()")
    public ProductInfo saveLocalCache(ProductInfo productInfo) {
        return productInfo;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'key_'+#id")
    public ProductInfo getLocalCache(Long id) {
        return null;
    }

    @Override
    @CachePut(value = CACHE_NAME, key = "'product_info_'+#productInfo.getId()")
    public ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo) {
        return productInfo;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'product_info_'+#productId")
    public ProductInfo getProductInfoFromLocalCache(Long productId) {
        return null;
    }

    @Override
    public void saveProductInfo2RedisCache(ProductInfo productInfo) {
        String key = "product_info_" + productInfo.getId();
        jedisCluster.set(key, JSONObject.toJSONString(productInfo));
    }

    @Override
    public ProductInfo getProductInfoFromRedisCache(ProductInfo productInfo) {
        return null;
    }

    @Override
    @CachePut(value = CACHE_NAME, key = "'shop_info_'+#shopInfo.getId()")
    public ShopInfo saveShopInfo2LocalCache(ShopInfo shopInfo) {
        return shopInfo;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'shop_info_'+#shopId")
    public ShopInfo getShopInfoFromLocalCache(Long shopId) {
        return null;
    }

    @Override
    public void saveShopInfo2RedisCache(ShopInfo shopInfo) {
        String key = "shop_info_" + shopInfo.getId();
        jedisCluster.set(key, JSONObject.toJSONString(shopInfo));
    }

    @Override
    public ShopInfo getShopInfoFromRedisCache(ShopInfo shopInfo) {
        return null;
    }

}
