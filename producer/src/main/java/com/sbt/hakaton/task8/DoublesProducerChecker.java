package com.sbt.hakaton.task8;

import com.google.common.base.Splitter;
import com.google.common.hash.Hashing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DoublesProducerChecker {
    private static final Logger LOG = LogManager.getLogger();

    @Value("${count.all:500000}")
    private long allCount;

    private Splitter splitter = Splitter.on(';').limit(3);

    public static void main(String[] args) throws IOException {
        new DoublesProducerChecker().checkForDoubles(Paths.get("/home/sergey/IdeaProjects/task8/log/consumer.log"));
    }


    public void checkForDoubles(Path log) throws IOException {
        long start = System.currentTimeMillis();
        Set<String> uniques = new HashSet<>(500_000);
        Set<String> notUniques = new HashSet<>(500_000);
        AtomicInteger nonUniqCounter = new AtomicInteger();
        Files.lines(log, StandardCharsets.UTF_8)
                .map(line -> splitter.splitToList(line).get(2))
                .map(value -> Hashing.murmur3_128().hashUnencodedChars(value).toString())
                .forEach(hash -> {
                    if (!uniques.add(hash)) {
                        notUniques.add(hash);
                        //LOG.error("Found not unique value: {}", value);
                        nonUniqCounter.incrementAndGet();
                    }
                });
        LOG.info("Uniques.size={}", uniques.size());
        LOG.info("Non unique values count: {}. Values: {}", notUniques.size(), notUniques);
        LOG.info("Non uniques.size={}", nonUniqCounter);
        LOG.info("TPS: {}", (uniques.size() + notUniques.size()) * 1000.0 / (System.currentTimeMillis() - start));
    }

    public void checkForDoubles1(Path log) throws IOException {
        Set<String> uniques = new HashSet<>(500_000);
        Set<String> notUniques = new HashSet<>(500_000);
        AtomicInteger nonUniqCounter = new AtomicInteger();
        Files.lines(log, StandardCharsets.UTF_8)
                .map(line -> splitter.splitToList(line).get(2))
                //.map(value -> value.substring(50, 100))
                .forEach(value -> {
                    if (!uniques.add(value.substring(50, 100))) {
                        notUniques.add(value);
                        //LOG.error("Found not unique value: {}", value);
                        nonUniqCounter.incrementAndGet();
                    }
                });
        LOG.info("Uniques.size={}", uniques.size());
        LOG.info("Non unique values: {}", notUniques);
        LOG.info("Non uniques.size={}", nonUniqCounter);
    }
}
