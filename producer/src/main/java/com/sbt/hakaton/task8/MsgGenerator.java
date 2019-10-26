package com.sbt.hakaton.task8;

import com.sbt.hakaton.task8.producer.SimpleProducer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MsgGenerator {
    private static final Logger LOG = LogManager.getLogger();

    private final SimpleProducer<String, String> producer;
    private final AtomicLong counter;
    private final AtomicLong startTime;

    @Value("${count.all:500000}")
    private long allCount;

    public MsgGenerator(
            SimpleProducer<String, String> producer) {
        this.producer = producer;
        counter = new AtomicLong();
        startTime = new AtomicLong();
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> LOG.info("Produced: {}", counter),
                        1, 1, TimeUnit.SECONDS);
    }

    public void generate() {
        LOG.info("Started generation");
        startTime.set(System.currentTimeMillis());
        counter.set(0);
        String dd = RandomStringUtils.randomAlphanumeric(9000, 10000);
        while (counter.get() < allCount) {
            int rnd = ThreadLocalRandom.current().nextInt(100);
            if (rnd > 75) {
                producer.produce(null, dd);
            } else {
                producer.produce(null, RandomStringUtils.randomAlphanumeric(9000, 10000));
            }
            counter.incrementAndGet();
        }
        LOG.info(stop());
    }

    public void load(Path file) throws IOException {
        LOG.info("Started loading");
        startTime.set(System.currentTimeMillis());
        counter.set(0);
        Files.lines(file, StandardCharsets.UTF_8)
                .map(line -> line.split(";")[2])
                .forEach(value -> {
                    producer.produce(null, value);
                });

    }

    private String stop() {
        long length = System.currentTimeMillis() - startTime.get();
        long tps = counter.get() / TimeUnit.MILLISECONDS.toSeconds(length);
        return String.format("Stopped. Generated: %s, time: %s, tps: %s", counter, TimeUnit.MILLISECONDS.toSeconds(length), tps);
    }

}
