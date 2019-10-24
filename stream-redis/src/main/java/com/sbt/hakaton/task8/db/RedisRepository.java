package com.sbt.hakaton.task8.db;

public interface RedisRepository {

    Boolean setIfAbsent(byte[] key, String value);
}
