package com.sbt.hakaton.task8.db;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RedisRepositoryImpl implements RedisRepository {
    private static final Logger LOG = LogManager.getLogger();

    private final List<RedisTemplate<String, String>> redisTemplates;
    private final List<ValueOperations<String, String>> operations;
    private final int redisCount;

    public RedisRepositoryImpl(List<RedisTemplate<String, String>> redisTemplates) {
        this.redisTemplates = redisTemplates;
        operations = new ArrayList<>(redisTemplates.size());
        for (RedisTemplate<String, String> redisTemplate : redisTemplates) {
            operations.add(redisTemplate.opsForValue());
        }
        redisCount = redisTemplates.size();
    }

    @Override
    public Stream<Boolean> setIfAbsent(List<Pair<String, String>> keyValues) {
        //LOG.error("node checked: {}", key.getBytes()[0] % redisCount);
        RedisTemplate<String, String> template = redisTemplates.get(0);
        List<Object> results = template.executePipelined((RedisCallback<?>) con -> {
            return null;
        });
        return results.stream().map(Boolean.class::cast);
    }
}
