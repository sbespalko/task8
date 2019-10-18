package com.sbt.hakaton.task8;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

public class DoublesChecker {
    private static final Logger LOG = LogManager.getLogger();

    private Set<String> uniques = new LinkedHashSet<>();

    @Test
    void checkFile() throws IOException {
        Path log = Paths.get("C:/Users/17239347/projects/task8/log/consumer.log");
        Files.lines(log, StandardCharsets.UTF_8)
                .map(line -> line.split(" ")[1])
                .forEach(value -> {
                    if (!uniques.add(value)) {
                        LOG.error("Found not unique value: {}", value);
                    }
                });
        LOG.info("Uniques.size={}", uniques.size());
    }
}
