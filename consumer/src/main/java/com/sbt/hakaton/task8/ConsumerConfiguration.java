package com.sbt.hakaton.task8;

import com.sbt.hakaton.task8.consumer.SimpleConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableKafka
public class ConsumerConfiguration {
    private static final Logger LOG = LogManager.getLogger();

    @Value("${messaging.kafka-dp.brokers.url:localhost:9092}")
    private String brokersUrl;

    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigurableApplicationContext ctx = SpringApplication.run(ConsumerConfiguration.class, args);
        SimpleConsumer consumer = ctx.getBean(SimpleConsumer.class);
        LOG.info("Read from topic: {}", consumer.resultTopic);
        try {
            while (!consumer.finished()) {
                long start = System.currentTimeMillis();
                long count = consumer.counter.get();
                synchronized (consumer) {
                    consumer.wait(1000);
                    LOG.info("Consumed msg: {}. TPS: {}",
                            consumer.counter,
                            (consumer.counter.get() - count) * 1000.0 / (System.currentTimeMillis() - start));

                }
            }
        } catch (InterruptedException e) {
            LOG.error("Interrupted", e);
        }
        ctx.close();
        LogManager.shutdown();
        TimeUnit.SECONDS.sleep(2);
        DoublesConsumerChecker doublesConsumerChecker = new DoublesConsumerChecker();
        doublesConsumerChecker.checkForDoubles(Paths.get("/home/sergey/IdeaProjects/task8/log/consumer.log"));
    }

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
        config.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        //config.put(ConsumerConfig.GROUP_ID_CONFIG, "team4developer");
        //config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put("security.protocol", "SSL");
        config.put("ssl.truststore.location", "/home/sergey/team4/team4developer.jks");
        config.put("ssl.truststore.password", "CVTqLn4nDjip");
        config.put("ssl.keystore.location", "/home/sergey/team4/team4developer.jks");
        config.put("ssl.keystore.password", "CVTqLn4nDjip");
        config.put("ssl.key.password", "CVTqLn4nDjip");
        config.put("ssl.endpoint.identification.algorithm", "");

    }

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
