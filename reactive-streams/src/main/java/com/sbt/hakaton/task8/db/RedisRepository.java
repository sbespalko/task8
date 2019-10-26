package com.sbt.hakaton.task8.db;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Stream;

public interface RedisRepository {

    Stream<Boolean> setIfAbsent(List<Pair<String, String>> keyValues);
}
