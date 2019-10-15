package com.sbt.hakaton.task8.config;

import com.sbt.hakaton.task8.http.MsgGenerator;
import com.sbt.hakaton.task8.producer.SimpleProducer;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
@ComponentScan(basePackageClasses = MsgGenerator.class)
public class ProducerConfiguration {

    @Value("${messaging.kafka-dp.brokers.url:localhost:9092}")
    private String brokersUrl;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> producerConfig = new HashMap<>();
        setDefaults(producerConfig);
        return new DefaultKafkaProducerFactory<>(producerConfig);
    }

    private void setDefaults(Map<String, Object> config) {
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokersUrl);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public SimpleProducer<String, String> simpleProducer() {
        return new SimpleProducer<>(kafkaTemplate());
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder()
                .namingPattern("generator-%d")
                .build());
    }
}
