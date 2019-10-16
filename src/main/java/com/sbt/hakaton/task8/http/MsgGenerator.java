package com.sbt.hakaton.task8.http;

import com.sbt.hakaton.task8.producer.SimpleProducer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.sbt.hakaton.task8.stream.Stream1.REDIS_EMULATION;

@Controller
public class MsgGenerator {
    private static final Logger LOG = LogManager.getLogger();

    private final ExecutorService executor;
    private final SimpleProducer<String, String> producer;
    private volatile boolean isRunning;

    public MsgGenerator(
            ExecutorService executor,
            SimpleProducer<String, String> producer) {
        this.executor = executor;
        this.producer = producer;
    }

    @GetMapping("/start/{frequency}")
    public void start(@PathVariable Integer frequency) {
        if (isRunning) {
            LOG.warn("First stop generation");
            return;
        }
        long delayNanos = TimeUnit.SECONDS.toNanos(1) / frequency;
        isRunning = true;
        executor.execute(() -> {
            try {
                while (isRunning) {
                    int rnd = ThreadLocalRandom.current().nextInt(100);
                    if (rnd > 70) {
                        String next;
                        if (REDIS_EMULATION.iterator().hasNext()) {
                            next = REDIS_EMULATION.iterator().next();
                        } else {
                            next = RandomStringUtils.randomAlphabetic(30, 500);
                        }
                        producer.produce(null, next);
                    } else {
                        producer.produce(null, RandomStringUtils.randomAlphabetic(30, 500));
                    }
                    TimeUnit.NANOSECONDS.sleep(delayNanos);
                }
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        });
    }

    @GetMapping("/stop")
    public void stop() {
        isRunning = false;
    }

}
