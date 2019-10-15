package com.sbt.hakaton.task8.config;

import com.sbt.hakaton.task8.consumer.SimpleConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class ConsumerConfiguration {
    @Value("${messaging.kafka-dp.brokers.url:localhost:9092}")
    private String brokersUrl;

    @Value("${topic.result}")
    private String resultTopic;

    @Bean
    ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> consumerConfig = new HashMap<>();
        setDefaults(consumerConfig);
        return new DefaultKafkaConsumerFactory<>(consumerConfig);
    }

    private void setDefaults(Map<String, Object> config) {
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokersUrl);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "simple-group");
    }

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    SimpleConsumer<String, String> simpleConsumer() {
        return new SimpleConsumer<>();
    }
}
