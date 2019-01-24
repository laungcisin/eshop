package com.laungcisin.eshop.storm.spout;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 从kafka中消费数据
 */
public class AccessLogKafkaSpout extends BaseRichSpout {
    private SpoutOutputCollector collector;
    private ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1000);
    private static final Logger logger = LoggerFactory.getLogger(AccessLogKafkaSpout.class);

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        startKafkaConsumer();
    }

    @Override
    public void nextTuple() {
        try {
            if (queue.size() > 0) {
                String message = queue.take();
                logger.info("[AccessLogKafkaSpout]从队列中获取一条消息，并发射出去：{}。", message);
                this.collector.emit(new Values(message));
            } else {
                Utils.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("message"));
    }

    private void startKafkaConsumer() {
        Properties props = new Properties();
        // 确保 rebalance.max.retries * rebalance.backoff.ms > zookeeper.session.timeout.ms
        props.put("zookeeper.connect", "192.168.33.61:2181,192.168.33.62:2181,192.168.33.63:2181");
        props.put("group.id", "eshop-cache-group");
        props.put("zookeeper.session.timeout.ms", "40000");
        props.put("zookeeper.connection.timeout.ms", "40000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("rebalance.backoff.ms", "20000");
        props.put("rebalance.max.retries", "10");
        props.put("auto.commit.interval.ms", "1000");
        ConsumerConfig consumerConfig = new ConsumerConfig(props);

        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(consumerConfig);
        String topic = "access-log";

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        for (final KafkaStream stream : streams) {
            new Thread(new KafkaMessageProcessor(stream)).start();
        }

    }

    private class KafkaMessageProcessor implements Runnable {
        private KafkaStream kafkaStream;

        private KafkaMessageProcessor(KafkaStream kafkaStream) {
            this.kafkaStream = kafkaStream;
        }

        @Override
        public void run() {
            ConsumerIterator<byte[], byte[]> it = kafkaStream.iterator();
            while (it.hasNext()) {
                String message = new String(it.next().message());

                logger.info("[AccessLogKafkaSpout]接收到一条信息，放入队列：{}。", message);
                try {
                    queue.put(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}