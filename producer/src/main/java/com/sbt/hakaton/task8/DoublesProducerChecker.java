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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DoublesProducerChecker {
    private static final Logger LOG = LogManager.getLogger();

    @Value("${count.all:500000}")
    private long allCount;

    private final ScheduledExecutorService statExecutor;
    private final Set<String> uniques = new HashSet<>(500_000);
    private final Set<String> notUniques = new HashSet<>(500_000);
    private Splitter splitter = Splitter.on(';').limit(2);

    public DoublesProducerChecker() {
        statExecutor = Executors.newSingleThreadScheduledExecutor();
        statExecutor.scheduleAtFixedRate(() -> LOG.info("Stat: uniq:{}, non-uniq:{}", uniques.size(), notUniques.size())
                , 1, 1, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws IOException {
        new DoublesProducerChecker().checkForDoubles(Paths.get("/home/sergey/IdeaProjects/task8/generated/producer.log"));
    }

    public void checkForDoubles(Path log) throws IOException {
        long start = System.currentTimeMillis();
        AtomicInteger nonUniqCounter = new AtomicInteger();
        Files.lines(log, StandardCharsets.UTF_8)
                .map(line -> splitter.splitToList(line).get(1))
                .map(value -> Hashing.murmur3_128().hashUnencodedChars(value).toString())
                .forEach(hash -> {
                    if (!uniques.add(hash)) {
                        notUniques.add(hash);
                        //LOG.error("Found not unique value: {}", value);
                        nonUniqCounter.incrementAndGet();
                    }
                });
        statExecutor.shutdown();
        LOG.info("Uniques.size={}", uniques.size());
        LOG.info("Non unique values count: {}. Values: {}", notUniques.size(), notUniques);
        LOG.info("Non uniques.size={}", nonUniqCounter);
        LOG.info("TPS: {}", (uniques.size() + notUniques.size()) * 1000.0 / (System.currentTimeMillis() - start));
    }
}
