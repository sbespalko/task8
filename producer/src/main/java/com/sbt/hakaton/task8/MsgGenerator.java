package com.sbt.hakaton.task8;

import com.sbt.hakaton.task8.producer.SimpleProducer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

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
    }

    public void work() {
        LOG.info("Started");
        startTime.set(System.currentTimeMillis());
        counter.set(0);
        String dd = RandomStringUtils.randomAlphanumeric(100, 1000);
        while (counter.get() < allCount) {
            int rnd = ThreadLocalRandom.current().nextInt(100);
            if (rnd > 75) {
                producer.produce(null, dd);
            } else {
                producer.produce(null, RandomStringUtils.randomAlphanumeric(100, 1000));
            }
            counter.incrementAndGet();
        }
        LOG.info(stop());
    }

    private String stop() {
        long length = System.currentTimeMillis() - startTime.get();
        long tps = counter.get() / TimeUnit.MILLISECONDS.toSeconds(length);
        return String.format("Stopped. Generated: %s, time: %s, tps: %s", counter, TimeUnit.MILLISECONDS.toSeconds(length), tps);
    }

}
