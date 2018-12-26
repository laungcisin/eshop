package com.laungcisin.eshop.cache.kafka;

import com.alibaba.fastjson.JSONObject;
import com.laungcisin.eshop.cache.model.ProductInfo;
import com.laungcisin.eshop.cache.model.ShopInfo;
import com.laungcisin.eshop.cache.service.CacheService;
import com.laungcisin.eshop.cache.spring.SpringContext;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

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
        String productInfoJSON = "{\"id\": 5, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:00:00\"}";

        ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);

        cacheService.saveProductInfo2LocalCache(productInfo);
        System.out.println("----------------------------: 获取刚保存的商品id：" + cacheService.getProductInfoFromLocalCache(productId));
        cacheService.saveProductInfo2RedisCache(productInfo);
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
