package com.laungcisin.eshop.cache.controller;

import com.alibaba.fastjson.JSONObject;
import com.laungcisin.eshop.cache.model.ProductInfo;
import com.laungcisin.eshop.cache.model.ShopInfo;
import com.laungcisin.eshop.cache.rebuild.RebuildCacheQueue;
import com.laungcisin.eshop.cache.service.CacheService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class CacheController {

    @Resource
    private CacheService cacheService;

    @RequestMapping("/testPutCache")
    @ResponseBody
    public void testPutCache(ProductInfo productInfo) {
        System.out.println(productInfo.getId() + ":" + productInfo.getName());
        cacheService.saveLocalCache(productInfo);
    }

    @RequestMapping("/testGetCache")
    @ResponseBody
    public ProductInfo testGetCache(Long id) {
        ProductInfo productInfo = cacheService.getLocalCache(id);
        System.out.println(productInfo.getId() + ":" + productInfo.getName());
        return productInfo;
    }

    @RequestMapping("/getProductInfo")
    @ResponseBody
    public ProductInfo getProductInfo(Long productId) {
        ProductInfo productInfo = null;

        // 1.从redis中获取数据
        productInfo = cacheService.getProductInfoFromRedisCache(productId);
        if (productInfo != null) {
            System.out.println("=================从redis中获取缓存，商品信息=" + productInfo);
        }

        if (productInfo == null) {
            // 2.从本地缓存中获取数据
            productInfo = cacheService.getProductInfoFromLocalCache(productId);
            if (productInfo != null) {
                System.out.println("=================从ehcache中获取缓存，商品信息=" + productInfo);
            }
        }

        if (productInfo == null) {
            // 3.从数据源重新拉取数据
            //TODO:从数据库中获取数据
            String productInfoJSON = "{\"id\": 11, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:10:00\"}";
            productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);

            RebuildCacheQueue rebuildCacheQueue = RebuildCacheQueue.getInstance();
            rebuildCacheQueue.addProductInfo(productInfo);
        }

        System.out.println("=================获取到商品信息=" + productInfo);
        return productInfo;
    }

    @RequestMapping("/getShopInfo")
    @ResponseBody
    public ShopInfo getShopInfo(Long shopId) {
        ShopInfo shopInfo = null;
        shopInfo = cacheService.getShopInfoFromRedisCache(shopId);

        if (shopInfo == null) {
            shopInfo = cacheService.getShopInfoFromLocalCache(shopId);
        }

        if (shopInfo == null) {
            //TODO:从数据库中获取数据
        }

        System.out.println(shopInfo.getId() + ":" + shopInfo.getName());
        return shopInfo;
    }
}