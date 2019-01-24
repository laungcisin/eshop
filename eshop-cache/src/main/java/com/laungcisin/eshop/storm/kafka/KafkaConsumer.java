package com.laungcisin.eshop.storm.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * kafka消费者
 */
public class KafkaConsumer implements Runnable {

    private final ConsumerConnector consumer;
    private final String topic;

    public KafkaConsumer(String topic) {
        consumer = Consumer.createJavaConsumerConnector(createConsumerConfig());
        this.topic = topic;
    }

    private static ConsumerConfig createConsumerConfig() {
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
        return new ConsumerConfig(props);
    }

    public void run() {
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        for (final KafkaStream stream : streams) {
            new Thread(new KafkaMessageProcessor(stream)).start();
        }
    }
}
