package com.sbt.hakaton.task8.db;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class RedisRepositoryImpl implements RedisRepository {

    private final RedisTemplate<byte[], String> redisTemplate;
    private final ValueOperations<byte[], String> operations;

    public RedisRepositoryImpl(RedisTemplate<byte[], String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        operations = redisTemplate.opsForValue();
    }

    @Override
    public Boolean setIfAbsent(byte[] key, String value) {
        return operations.setIfAbsent(key, value);
    }
}
