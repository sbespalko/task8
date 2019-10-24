package com.sbt.hakaton.task8.stream;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sbt.hakaton.task8.db.RedisRepository;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class StreamStarter {
    private static final Logger LOG = LogManager.getLogger();

    private final RedisRepository redis;
    private final StreamsBuilder streamsBuilder;

    @Value("${topic.initial}")
    private String initialTopic;

    @Value("${topic.result}")
    private String resultTopic;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public final AtomicLong counter = new AtomicLong();
    //пачка коннектов к базам редис. почитать про кластеризацию
    //private RedisRepository<String>[] connections;
    private final int stringToHashCacheSize = 1_000;

    //    @Value("${redis.partitions:5}")
//    private int partitionsCount;
    private final int localCacheSize = 1_000_000_000;
    @Value("${count.all:500000}")
    private long allCount;
    private Map<String, byte[]> stringToHashCache = Maps.newHashMapWithExpectedSize(stringToHashCacheSize);
    private Set<byte[]> localCache = Sets.newHashSetWithExpectedSize(localCacheSize);

    public StreamStarter(RedisRepository redis, StreamsBuilder streamsBuilder) {
        this.redis = redis;
        this.streamsBuilder = streamsBuilder;
        scheduler.scheduleAtFixedRate(() -> {
            if (stringToHashCache.size() > stringToHashCacheSize) {
                stringToHashCache.clear();
            }
            if (localCache.size() > localCacheSize) {
                localCache.clear();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public KStream<String, String> startProcessing() {
        KStream<String, String> stream = streamsBuilder.stream(initialTopic, Consumed.with(Serdes.String(), Serdes.String()));
        AtomicBoolean first = new AtomicBoolean(true);
        AtomicLong start = new AtomicLong();
        AtomicLong finish = new AtomicLong();
        stream
                .filter((key, value) -> {
                    //TODO Обязательно нужно применить BATCHing на Redis- без этого не будет производительности
                    if (first.get()) {
                        LOG.warn("Started");
                        first.set(false);
                        start.set(System.currentTimeMillis());
                    }
                    if (counter.incrementAndGet() >= allCount) {
                        finish.set(System.currentTimeMillis());
                        LOG.warn("Finished: {}", counter);
                        LOG.warn("Handled: {}, TPS: {}", counter, counter.get() * 1000.0 / (finish.get() - start.get()));

                    }
                    byte[] hash = getHash(value);
                    if (localCache.add(hash)) {
                        //int partitionNum = hash[0] % partitionsCount;
                        return redis.setIfAbsent(hash, value);
                        //return connections[partitionNum].putIfAbsent(hash, value);
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

    public boolean finished() {
        boolean finished = counter.get() >= allCount;
        if (finished) {
            scheduler.shutdownNow();
        }
        return finished;
    }
}
