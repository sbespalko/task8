package com.sbt.hakaton.task8;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DoublesConsumerChecker {
    private static final Logger LOG = LogManager.getLogger();

    @Value("${count.all:500000}")
    private long allCount;

    private Set<String> uniques = new LinkedHashSet<>();
    private Set<String> notUniques = new LinkedHashSet<>();

    public static void main(String[] args) throws IOException {
        new DoublesConsumerChecker().checkForDoubles(Paths.get("/home/sergey/IdeaProjects/task8/log/consumer.1.log"));
    }

    public void checkForDoubles(Path log) throws IOException {
        AtomicInteger nonUniqCounter = new AtomicInteger();
        Files.lines(log, StandardCharsets.UTF_8)
                .map(line -> line.split(";")[2])
                .forEach(value -> {
                    if (!uniques.add(value)) {
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
