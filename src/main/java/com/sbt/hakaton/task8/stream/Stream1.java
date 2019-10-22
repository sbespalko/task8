package com.sbt.hakaton.task8.stream;

import com.sbt.hakaton.task8.db.RedisRepository;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class Stream1 {

    private final RedisRepository<String> redis;

    @Value("${topic.initial}")
    private String initialTopic;

    @Value("${topic.result}")
    private String resultTopic;

    @Value("${redis.partitions:5}")
    private int partitionsCount;

    //пачка коннектов к базам редис. почитать про кластеризацию
    private RedisRepository<String>[] connections;

    //Мапа, удаляющая старые записи по мере переполнения
    private Map<String, byte[]> stringToHashCache = new LinkedHashMap<String, byte[]>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return this.size() > 100;
        }
    };

    //кеш хешей. Безразмерный
    private Set<byte[]> localCache = new HashSet<>();

    public Stream1(RedisRepository<String> redis) {
        this.redis = redis;
    }

    @Bean
    public KStream<String, String> startProcessing(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream(initialTopic, Consumed.with(Serdes.String(), Serdes.String()));
        stream
                .filter((key, value) -> {
                    //TODO Обязательно нужно применить BATCHing на Redis- без это не будет производительности
                    byte[] hash = getHash(value);
                    if (localCache.add(hash)) {
                        int partitionNum = hash[0] % partitionsCount;
                        return connections[partitionNum].putIfAbsent(hash, value);
                    } else {
                        return false;
                    }
                })
                .to(resultTopic, Produced.with(Serdes.String(), Serdes.String()));
        return stream;
    }

    private byte[] getHash(String key) {
        return stringToHashCache.compute(key,
                (k, v) -> v == null
                        //TODO функция хеширования - вставить нормальную. Мб вообще кешить не надо, если эта и так быстрая
                        ? Arrays.copyOf(key.getBytes(StandardCharsets.UTF_8), 16)
                        : v);
    }
}
