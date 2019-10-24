package com.sbt.hakaton.task8.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class SimpleConsumer<K, V> {
    private static final Logger LOG = LogManager.getLogger();

    public final AtomicLong counter = new AtomicLong();

    @Value("${count.uniques:380735}")
    private long allCount;
    @Value("${topic.result}")
    public String resultTopic;

    @KafkaListener(topics = "${topic.result}")
    public void consume(ConsumerRecord<K, V> record) {
        //LOG.debug("from:{}, key:{}, value:{}", resultTopic, record.key(), record.value());
        LOG.debug("{};{};{}", counter.getAndIncrement(), record.timestamp(), record.value());
    }

    public boolean finished() {
        return counter.get() >= allCount;
    }
}
