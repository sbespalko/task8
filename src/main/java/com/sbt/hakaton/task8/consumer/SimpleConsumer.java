package com.sbt.hakaton.task8.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;

public class SimpleConsumer<K, V> {
    private static final Logger LOG = LogManager.getLogger();

    @Value("${topic.result}")
    private String resultTopic;

    @KafkaListener(topics = "${topic.result}")
    public void consume(ConsumerRecord<K, V> record) {
        //LOG.debug("from:{}, key:{}, value:{}", resultTopic, record.key(), record.value());
        LOG.debug(record.value());
    }
}
