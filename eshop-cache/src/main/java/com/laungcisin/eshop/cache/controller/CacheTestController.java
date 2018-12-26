package com.laungcisin.eshop.cache.controller;

import com.laungcisin.eshop.cache.model.ProductInfo;
import com.laungcisin.eshop.cache.service.CacheService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class CacheTestController {

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

}