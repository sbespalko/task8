package com.sbt.hakaton.task8.http;

import com.sbt.hakaton.task8.producer.SimpleProducer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class MsgGenerator {
    private static final Logger LOG = LogManager.getLogger();

    private final ExecutorService executor;
    private final SimpleProducer<String, String> producer;
    private final AtomicLong counter;
    private final AtomicLong startTime;
    private volatile boolean isRunning;

    public MsgGenerator(
            ExecutorService executor,
            SimpleProducer<String, String> producer) {
        this.executor = executor;
        this.producer = producer;
        counter = new AtomicLong();
        startTime = new AtomicLong();
    }

    @GetMapping("/start/{frequency}")
    public String start(@PathVariable Integer frequency) {
        if (isRunning) {
            return "First stop generation";
        }
        long delayNanos = TimeUnit.SECONDS.toNanos(1) / frequency;
        isRunning = true;
        executor.execute(() -> {
            try {
                startTime.set(System.currentTimeMillis());
                counter.set(0);
                String dd = RandomStringUtils.randomAlphabetic(30, 500);
                while (isRunning) {
                    int rnd = ThreadLocalRandom.current().nextInt(100);
                    if (rnd > 70) {
                        producer.produce(null, dd);
                    } else {
                        producer.produce(null, String.valueOf(ThreadLocalRandom.current().nextLong()));
                    }
                    if (counter.incrementAndGet() == 10000) {
                        TimeUnit.NANOSECONDS.sleep(delayNanos * 10000);
                    }
                }
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        });
        return "Started";
    }

    @GetMapping("/stop")
    public String stop() {
        isRunning = false;
        long length = System.currentTimeMillis() - startTime.get();
        long tps = counter.get() / TimeUnit.MILLISECONDS.toSeconds(length);
        return String.format("Stopped. Generated: %s, time: %s, tps: %s", counter, TimeUnit.MILLISECONDS.toSeconds(length), tps);
    }

}
