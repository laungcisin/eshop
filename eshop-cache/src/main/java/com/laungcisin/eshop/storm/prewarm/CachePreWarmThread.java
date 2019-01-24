package com.laungcisin.eshop.storm.prewarm;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.laungcisin.eshop.storm.model.ProductInfo;
import com.laungcisin.eshop.storm.service.CacheService;
import com.laungcisin.eshop.storm.spring.SpringContext;
import com.laungcisin.eshop.storm.zk.ZooKeeperSession;
import org.springframework.util.StringUtils;

/**
 * 缓存预热线程
 */

public class CachePreWarmThread extends Thread {
    private static final String TASK_ID_LIST = "/taskId-list";
    private static final String TASK_ID_LOCK_PREFIX = "/taskId-lock-";
    private static final String TASK_ID_LOCK_STATUS_PREFIX = "/taskId-status-lock-";
    private static final String TASK_ID_STATUS_PREFIX = "/taskId-status-";
    private static final String TASK_HOT_PRODUCT_LIST_PREFIX = "/task-hot-product-list-";

    @Override
    public void run() {
        CacheService cacheService = SpringContext.getApplicationContext().getBean("cacheService", CacheService.class);
        ZooKeeperSession zooKeeperSession = ZooKeeperSession.getInstance();

        /**
        预热步骤：
        1、服务启动的时候，进行缓存预热
        2、从zk中读取taskId列表
        3、依次遍历每个taskId，尝试获取分布式锁，如果获取不到，快速报错，不要等待，因为说明已经有其他服务实例在预热了
        4、直接尝试获取下一个taskId的分布式锁
        5、即使获取到了分布式锁，也要检查一下这个taskId的预热状态，如果已经被预热过了，就不再预热了
        6、执行预热操作，遍历productId列表，查询数据，然后写ehcache和redis
        7、预热完成后，设置taskId对应的预热状态
         */
        // 2、从zk中读取taskId列表
        String taskIdList = zooKeeperSession.getNodeData(TASK_ID_LIST);
        if (!StringUtils.isEmpty(taskIdList)) {
            String[] taskIdListSplit = taskIdList.split(",");

            // 3、依次遍历每个taskId，尝试获取分布式锁，如果获取不到，快速报错，不要等待，因为说明已经有其他服务实例在预热了
            for (String taskId : taskIdListSplit) {
                String taskIdLockPath = TASK_ID_LOCK_PREFIX + taskId;

                // 尝试获取分布式锁
                boolean result = zooKeeperSession.acquireFastFailedDistributedLock(taskIdLockPath);
                // 4、直接尝试获取下一个taskId的分布式锁
                if (!result) {
                    continue;
                }

                // 5、即使获取到了分布式锁，也要检查一下这个taskId的预热状态，如果已经被预热过了，就不再预热了
                String taskIdStatusLockPath = TASK_ID_LOCK_STATUS_PREFIX + taskId;
                zooKeeperSession.acquireDistributedLock(taskIdStatusLockPath);

                String taskIdStatus = zooKeeperSession.getNodeData(TASK_ID_STATUS_PREFIX + taskId);
                // 如果未被预热过
                if (StringUtils.isEmpty(taskIdStatus)) {
                    // 6、执行预热操作，遍历productId列表，查询数据，然后写ehcache和redis
                    String productIdList = zooKeeperSession.getNodeData(TASK_HOT_PRODUCT_LIST_PREFIX + taskId);
                    JSONArray productIdJSONArray = JSONArray.parseArray(productIdList);

                    for (int i = 0; i < productIdJSONArray.size(); i++) {
                        Long productId = productIdJSONArray.getLong(i);

                        // FIXME:模拟数据
                        String productInfoJSON = "{\"id\": " + productId + ", \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:10:00\"}";
                        ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);

                        // 写ehcache和redis
                        cacheService.saveProductInfo2LocalCache(productInfo);
                        cacheService.saveProductInfo2RedisCache(productInfo);
                    }

                    // 7、预热完成后，设置taskId对应的预热状态
                    zooKeeperSession.createNode(TASK_ID_STATUS_PREFIX + taskId);
                    zooKeeperSession.setNodeData(TASK_ID_STATUS_PREFIX + taskId, "success");
                }

                zooKeeperSession.releaseDistributedLock(taskIdStatusLockPath);
                zooKeeperSession.releaseDistributedLock(taskIdLockPath);
            }

        }
    }
}
