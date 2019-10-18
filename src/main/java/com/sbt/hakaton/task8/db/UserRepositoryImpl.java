package com.sbt.hakaton.task8.db;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements RedisRepository<String> {


    private HashOperations<byte[], Object, Object> hashOperations;

    public UserRepositoryImpl(RedisTemplate<byte[], String> redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public Boolean putIfAbsent(String value) {
        return hashOperations.putIfAbsent(getHash(value), value, value);
    }

    private static byte[] getHash(String value) {
        return value.getBytes();
    }

}
