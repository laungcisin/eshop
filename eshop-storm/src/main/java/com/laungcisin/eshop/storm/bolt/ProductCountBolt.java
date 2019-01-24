package com.laungcisin.eshop.storm.bolt;

import com.alibaba.fastjson.JSONArray;
import com.laungcisin.eshop.storm.zk.ZooKeeperSession;
import org.apache.storm.shade.org.apache.commons.lang.StringUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.trident.util.LRUMap;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 商品访问次数统计bolt
 */
public class ProductCountBolt extends BaseRichBolt {
    private static final Logger logger = LoggerFactory.getLogger(ProductCountBolt.class);
    private static final String TASK_ID_LIST = "/taskId-list";
    private static final String TASK_ID_LOCK_PREFIX = "/taskId-lock-";
    private static final String TASK_ID_LOCK_STATUS_PREFIX = "/taskId-status-lock-";
    private static final String TASK_ID_STATUS_PREFIX = "/taskId-status-";
    private static final String TASK_HOT_PRODUCT_LIST_PREFIX = "/task-hot-product-list-";

    private OutputCollector collector;
    private LRUMap<Long, Long> productCountMap = new LRUMap<>(1000);
    private ZooKeeperSession zooKeeperSession;
    private int taskId;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.zooKeeperSession = ZooKeeperSession.getInstance();
        this.taskId = context.getThisTaskId();
        this.collector = collector;

        new Thread(new ProductCountThread()).start();

        // 1、将storm自己运行的task的 taskId 写入一个 zookeeper node 中，形成 taskId 的列表
        // 2、然后每次都将自己的热门商品列表，写入自己的 taskId 对应的 zookeeper 节点
        // 3、然后这样的话，并行的预热程序才能从第一步中知道，有哪些 taskId
        // 4、然后并行预热程序根据每个 taskId 去获取一个锁，然后再从对应的 zNode 中拿到热门商品列表

        initTaskId(context.getThisTaskId());
    }

    private void initTaskId(int taskId) {
        zooKeeperSession.acquireDistributedLock();

        zooKeeperSession.createNode(TASK_ID_LIST);
        String taskIdList = zooKeeperSession.getNodeData();
        if (!StringUtils.isEmpty(taskIdList)) {
            taskIdList += ("," + taskId);
        } else {
            taskIdList += taskId;
        }

        zooKeeperSession.setNodeData(TASK_ID_LIST, taskIdList);
        zooKeeperSession.releaseDistributedLock();
    }

    private class ProductCountThread implements Runnable {
        // LRUMap中TopN热门商品列表的算法
        @Override
        public void run() {
            List<Map.Entry<Long, Long>> topnProductList = new ArrayList<Map.Entry<Long, Long>>();
            List<Long> productidList = new ArrayList<Long>();

            while (true) {
                try {
                    topnProductList.clear();
                    productidList.clear();

                    int topn = 3;

                    if (productCountMap.size() == 0) {
                        Utils.sleep(100);
                        continue;
                    }

                    for (Map.Entry<Long, Long> productCountEntry : productCountMap.entrySet()) {
                        if (topnProductList.size() == 0) {
                            topnProductList.add(productCountEntry);
                        } else {
                            // 比较大小，生成最热topn的算法有很多种
                            // 但是我这里为了简化起见，不想引入过多的数据结构和算法的的东西
                            // 很有可能还是会有漏洞，但是我已经反复推演了一下了，而且也画图分析过这个算法的运行流程了
                            boolean bigger = false;

                            for (int i = 0; i < topnProductList.size(); i++) {
                                Map.Entry<Long, Long> topnProductCountEntry = topnProductList.get(i);

                                if (productCountEntry.getValue() > topnProductCountEntry.getValue()) {
                                    int lastIndex = topnProductList.size() < topn ? topnProductList.size() - 1 : topn - 2;
                                    for (int j = lastIndex; j >= i; j--) {
                                        if (j + 1 == topnProductList.size()) {
                                            topnProductList.add(null);
                                        }
                                        topnProductList.set(j + 1, topnProductList.get(j));
                                    }
                                    topnProductList.set(i, productCountEntry);
                                    bigger = true;
                                    break;
                                }
                            }

                            if (!bigger) {
                                if (topnProductList.size() < topn) {
                                    topnProductList.add(productCountEntry);
                                }
                            }
                        }
                    }

                    // 获取到一个topn list
                    for (Map.Entry<Long, Long> topnProductEntry : topnProductList) {
                        productidList.add(topnProductEntry.getKey());
                    }

                    String topnProductListJSON = JSONArray.toJSONString(productidList);
                    zooKeeperSession.createNode("/task-hot-product-list-" + taskId);
                    zooKeeperSession.setNodeData("/task-hot-product-list-" + taskId, topnProductListJSON);
                    logger.info("【ProductCountThread计算出一份top3热门商品列表】zk path=" + ("/task-hot-product-list-" + taskId) + ", topnProductListJSON=" + topnProductListJSON);

                    Utils.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void execute(Tuple input) {
        Long productId = input.getLongByField("productId");

        logger.info("[ProductCountBolt]接收到一条信息， 产品id：{}。", productId);

        Long count = productCountMap.get(productId);
        if (count == null) {
            count = 0L;
        }

        count++;
        productCountMap.put(productId, count);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("productId"));
    }
}
