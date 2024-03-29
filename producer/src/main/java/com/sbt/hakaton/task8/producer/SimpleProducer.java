package com.sbt.hakaton.task8.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleProducer<K, V> {
    private static final Logger LOG = LogManager.getLogger();

    @Value("${topic.initial}")
    private String initialTopic;

    private final KafkaTemplate<K, V> kafkaTemplate;
    private final AtomicLong counter = new AtomicLong();

    public SimpleProducer(KafkaTemplate<K, V> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produce(K key, V value) {
        //LOG.debug("to:{}, key:{}, value:{}", initialTopic, key, value);
        LOG.debug("{};{}", counter.incrementAndGet(), value);
        kafkaTemplate.send(initialTopic, key, value);
    }
}
