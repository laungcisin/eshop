package com.laungcisin.eshop.cache.rebuild;

import com.laungcisin.eshop.cache.model.ProductInfo;
import com.laungcisin.eshop.cache.service.CacheService;
import com.laungcisin.eshop.cache.spring.SpringContext;
import com.laungcisin.eshop.cache.zk.ZooKeeperSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RebuildCacheThread implements Runnable {
    @Override
    public void run() {
        RebuildCacheQueue rebuildCacheQueue = RebuildCacheQueue.getInstance();
        ZooKeeperSession zooKeeperSession = ZooKeeperSession.getInstance();
        CacheService cacheService = SpringContext.getApplicationContext().getBean("cacheService", CacheService.class);

        while (true) {
            ProductInfo productInfo = rebuildCacheQueue.takeProductInfo();
            Long productId = productInfo.getId();
            zooKeeperSession.acquireDistributedLock(productId);// 获取zk分布式锁

            // 获取锁，从redis中获取数据
            ProductInfo existedProductInfo = cacheService.getProductInfoFromRedisCache(productId);
            if (existedProductInfo != null) {
                try {
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                    String productInfoModifiedTime = productInfo.getModifiedTime();
                    String existedProductInfoModifiedTime = existedProductInfo.getModifiedTime();

                    LocalDateTime time1 = LocalDateTime.parse(productInfoModifiedTime, df);
                    LocalDateTime time2 = LocalDateTime.parse(existedProductInfoModifiedTime, df);

                    if (time1.isBefore(time2)) {
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("current date[" + productInfo.getModifiedTime() + "] is after existed date[" + existedProductInfo.getModifiedTime() + "]");
            } else {
                System.out.println("重建缓存服务：redis中不存在productId:[" + productId + "]的数据");
            }

            cacheService.saveProductInfo2LocalCache(productInfo);
            cacheService.saveProductInfo2RedisCache(productInfo);
            zooKeeperSession.releaseDistributedLock(productId);
        }

    }
}
