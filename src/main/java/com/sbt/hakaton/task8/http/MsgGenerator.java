package com.sbt.hakaton.task8.http;

import com.sbt.hakaton.task8.producer.SimpleProducer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Controller
public class MsgGenerator {
    private static final Logger LOG = LogManager.getLogger();

    private final ScheduledExecutorService executor;
    private final SimpleProducer<String, String> producer;
    private ScheduledFuture<?> future;

    public MsgGenerator(
            ScheduledExecutorService executor,
            SimpleProducer<String, String> producer) {
        this.executor = executor;
        this.producer = producer;
    }

    @GetMapping("/start/{frequency}")
    public void start(@PathVariable Integer frequency) {
        if (future != null && !future.isCancelled()) {
            LOG.warn("First stop generation");
        }
        long delayNanos = TimeUnit.SECONDS.toNanos(1) / frequency;
        future = executor.scheduleAtFixedRate(() -> {
            producer.produce(null, RandomStringUtils.randomAlphabetic(30, 500));
        }, 0, delayNanos, TimeUnit.NANOSECONDS);
    }

    @GetMapping("/stop")
    public void stop() {
        future.cancel(true);
    }

}
