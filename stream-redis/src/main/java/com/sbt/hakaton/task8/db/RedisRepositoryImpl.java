package com.sbt.hakaton.task8.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;

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
    public Boolean setIfAbsent(String key, String value) {
        //LOG.error("node checked: {}", key.getBytes()[0] % redisCount);
        return operations.get(key.charAt(10) % redisCount).setIfAbsent(key, value);
    }
}
