package com.sbt.hakaton.task8.starter;

import com.sbt.hakaton.task8.config.ConsumerConfiguration;
import com.sbt.hakaton.task8.config.ProducerConfiguration;
import com.sbt.hakaton.task8.stream.Stream1;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.FailOnInvalidTimestamp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(scanBasePackageClasses = {
        ProducerConfiguration.class
        , ConsumerConfiguration.class
        , Stream1.class
})
public class Task8Application {

    @Value("${delivery-stats.stream.threads:1}")
    private int threads;

    @Value("${delivery-stats.kafka.replication-factor:1}")
    private int replicationFactor;

    @Value("${messaging.kafka-dp.brokers.url:localhost:9092}")
    private String brokersUrl;

    @Value("${kafka-streams.base-dir:tmp/stream}")
    private String stateDir;

    public static void main(String[] args) {
        SpringApplication.run(Task8Application.class, args);
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public StreamsConfig kStreamsConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "default");
        setDefaults(config);
        return new StreamsConfig(config);
    }


    private void setDefaults(Map<String, Object> config) {
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokersUrl);
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, FailOnInvalidTimestamp.class);
        config.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
    }

    @Bean
    public StreamsBuilderFactoryBean app1StreamBuilderFactoryBean() {
        Map<String, Object> config = new HashMap<>();
        setDefaults(config);
        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream1");
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 30000);
        config.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, threads);
        config.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, replicationFactor);
        return new StreamsBuilderFactoryBean(new KafkaStreamsConfiguration(config));

    }
}
