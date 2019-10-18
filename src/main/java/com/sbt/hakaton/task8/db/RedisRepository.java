package com.sbt.hakaton.task8.db;

public interface RedisRepository<T> {
    Boolean putIfAbsent(String value);
}
