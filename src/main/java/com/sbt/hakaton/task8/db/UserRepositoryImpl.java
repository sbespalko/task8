package com.sbt.hakaton.task8.db;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements RedisRepository<String> {


    private HashOperations<String, Object, Object> hashOperations;

    public UserRepositoryImpl(RedisTemplate<String, String> redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public Boolean putIfAbsent(byte[] key, String value) {
        return hashOperations.putIfAbsent("DOCS", key, null);
    }
}
