package com.sbt.hakaton.task8.starter;

import com.sbt.hakaton.task8.config.RedisConfiguration;
import com.sbt.hakaton.task8.db.RedisRepository;
import com.sbt.hakaton.task8.stream.StreamStarter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.FailOnInvalidTimestamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(scanBasePackageClasses = RedisConfiguration.class)
public class StreamConfiguration {
    private static final Logger LOG = LogManager.getLogger();

    @Value("${delivery-stats.stream.threads:1}")
    private int threads;

    @Value("${delivery-stats.kafka.replication-factor:1}")
    private int replicationFactor;

    @Value("${messaging.kafka-dp.brokers.url:localhost:9092}")
    private String brokersUrl;

    @Value("${kafka-streams.base-dir:stream/tmp}")
    private String stateDir;

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext ctx = SpringApplication.run(StreamConfiguration.class, args);
        //new Scanner(System.in).nextLine();
        StreamStarter streamStarter = ctx.getBean(StreamStarter.class);
        streamStarter.startProcessing();
        try {
            while (!streamStarter.finished()) {
                synchronized (streamStarter) {
                    streamStarter.wait(1000);
                    LOG.info("Streamed msg: {}", streamStarter.counter);

                }
            }
        } catch (InterruptedException e) {
            LOG.error("Interrupted", e);
        }
        ctx.close();
        LogManager.shutdown();
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
    public StreamsBuilderFactoryBean streamBuilderFactoryBean() {
        Map<String, Object> config = new HashMap<>();
        setDefaults(config);
        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream1");
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 30000);
        config.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, threads);
        config.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, replicationFactor);
        return new StreamsBuilderFactoryBean(new KafkaStreamsConfiguration(config));

    }

    @Bean
    public StreamStarter starter(RedisRepository repository, StreamsBuilder streamBuilder) {
        return new StreamStarter(repository, streamBuilder);
    }
}
