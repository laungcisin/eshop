package com.laungcisin.eshop.storm;

import com.laungcisin.eshop.storm.bolt.LogParseBolt;
import com.laungcisin.eshop.storm.bolt.ProductCountBolt;
import com.laungcisin.eshop.storm.spout.AccessLogKafkaSpout;
import com.laungcisin.eshop.storm.zk.ZooKeeperSession;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;

/**
 * 热门商品统计Topology
 */
public class HotProductTopology {

    public static void main(String[] args) {
        // TODO:此段代码，可以考虑删除
        String TASK_ID_LIST = "/taskId-list";
        String TASK_HOT_PRODUCT_LIST_PREFIX = "/task-hot-product-list-";
        ZooKeeperSession zooKeeperSession = ZooKeeperSession.getInstance();
        zooKeeperSession.acquireDistributedLock();
        zooKeeperSession.createNode(TASK_ID_LIST);
        zooKeeperSession.setNodeData(TASK_ID_LIST, "");
        zooKeeperSession.createNode(TASK_HOT_PRODUCT_LIST_PREFIX);
        zooKeeperSession.setNodeData(TASK_HOT_PRODUCT_LIST_PREFIX, "");
        zooKeeperSession.releaseDistributedLock();

        TopologyBuilder builder = new TopologyBuilder();
        // 1. spout
        builder.setSpout("AccessLogKafkaSpout", new AccessLogKafkaSpout(), 1);

        // 2.bolt
        builder.setBolt("LogParseBolt", new LogParseBolt(), 2)
                .setNumTasks(2)
                .shuffleGrouping("AccessLogKafkaSpout");

        builder.setBolt("ProductCountBolt", new ProductCountBolt(), 2)
                .setNumTasks(2)
                .fieldsGrouping("LogParseBolt", new Fields("productId"));

        Config config = new Config();

        if (args != null && args.length > 0) {
            config.setNumWorkers(3);
            try {
                StormSubmitter.submitTopology(args[0], config, builder.createTopology());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology("HotProductTopology", config, builder.createTopology());
            Utils.sleep(30000);
            localCluster.shutdown();
        }
    }
}
