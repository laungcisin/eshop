package com.laungcisin.eshop.storm.kafka;

import com.alibaba.fastjson.JSONObject;
import com.laungcisin.eshop.storm.model.ProductInfo;
import com.laungcisin.eshop.storm.model.ShopInfo;
import com.laungcisin.eshop.storm.service.CacheService;
import com.laungcisin.eshop.storm.spring.SpringContext;
import com.laungcisin.eshop.storm.zk.ZooKeeperSession;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * kafka消息处理线程
 */
public class KafkaMessageProcessor implements Runnable {
    private KafkaStream kafkaStream;

    private CacheService cacheService;

    public KafkaMessageProcessor(KafkaStream kafkaStream) {
        this.kafkaStream = kafkaStream;
        this.cacheService = SpringContext.getApplicationContext().getBean("cacheService", CacheService.class);
    }

    @Override
    public void run() {
        ConsumerIterator<byte[], byte[]> it = kafkaStream.iterator();
        while (it.hasNext()) {
            String message = new String(it.next().message());

            // message 转 json
            JSONObject messageJSONObject = JSONObject.parseObject(message);

            String serviceId = messageJSONObject.getString("serviceId");

            if ("productInfoService".endsWith(serviceId)) {
                processProductInfoChangeMessage(messageJSONObject);
            } else if ("shopInfoService".endsWith(serviceId)) {
                processShopInfoChangeMessage(messageJSONObject);
            }
        }
    }

    /**
     * 处理商品信息变更的消息
     *
     * @param messageJSONObject
     */
    private void processProductInfoChangeMessage(JSONObject messageJSONObject) {
        // 提取商品id
        Long productId = messageJSONObject.getLong("productId");

        // TODO：模拟获取数据库信息
        String productInfoJSON = "{\"id\": 11, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:00:00\"}";
        ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);

        // 获取zk分布式锁
        ZooKeeperSession zooKeeperSession = ZooKeeperSession.getInstance();
        zooKeeperSession.acquireDistributedLock(productId);

        // 获取锁后，从redis中获取数据
        ProductInfo existedProductInfo = cacheService.getProductInfoFromRedisCache(productId);
        if (existedProductInfo != null) {
            try {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                String productInfoModifiedTime = productInfo.getModifiedTime();
                String existedProductInfoModifiedTime = existedProductInfo.getModifiedTime();

                LocalDateTime time1 = LocalDateTime.parse(productInfoModifiedTime, df);
                LocalDateTime time2 = LocalDateTime.parse(existedProductInfoModifiedTime, df);

                if (time1.isBefore(time2)) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Kafka消息服务：redis中不存在productId:[" + productInfo.getId() + "]的数据");
        }

        // FIXME:模拟多线程访问：更新商品信息的同时，有缓存服务去获取商品信息(缓存重建)
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
        }

        cacheService.saveProductInfo2LocalCache(productInfo);
        System.out.println("----------------------------: 获取刚保存的商品id：" + cacheService.getProductInfoFromLocalCache(productId));
        cacheService.saveProductInfo2RedisCache(productInfo);
        // 释放分布式锁
        zooKeeperSession.releaseDistributedLock(productId);
    }

    /**
     * 处理店铺信息变更的消息
     *
     * @param messageJSONObject
     */
    private void processShopInfoChangeMessage(JSONObject messageJSONObject) {
        // 提取商品id
        Long productId = messageJSONObject.getLong("productId");
        Long shopId = messageJSONObject.getLong("shopId");

        // TODO：模拟获取数据库信息
        String shopInfoJSON = "{\"id\": 1, \"name\": \"小王的手机店\", \"level\": 5, \"goodCommentRate\":0.99}";

        ShopInfo shopInfo = JSONObject.parseObject(shopInfoJSON, ShopInfo.class);
        cacheService.saveShopInfo2LocalCache(shopInfo);
        System.out.println("----------------------------: 获取刚保存的店铺id：" + cacheService.getShopInfoFromLocalCache(shopId));
        cacheService.saveShopInfo2RedisCache(shopInfo);
    }
}
