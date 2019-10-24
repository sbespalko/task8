package com.sbt.hakaton.task8;

import com.sbt.hakaton.task8.producer.SimpleProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ProducerConfiguration {
    @Value("${messaging.kafka-dp.brokers.url:localhost:9092}")
    private String brokersUrl;

    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(ProducerConfiguration.class, args);
        context.getBean(MsgGenerator.class).work();
        context.close();
        LogManager.shutdown();
        TimeUnit.SECONDS.sleep(2);
        new DoublesProducerChecker().checkForDoubles(Paths.get("/home/sergey/IdeaProjects/task8/log/producer.log"));
    }

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
    public MsgGenerator generator() {
        return new MsgGenerator(simpleProducer());
    }
}
