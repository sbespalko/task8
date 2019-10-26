package com.sbt.hakaton.task8;

import com.google.common.hash.Hashing;
import com.sbt.hakaton.task8.db.RedisRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class Pipe {
    private static final Logger LOG = LogManager.getLogger();
    private final RedisRepository redisRepository;
    private final Flux<ReceiverRecord<String, String>> receiver;
    private final KafkaSender<String, String> sender;
    @Value("${topic.result}")
    public String resultTopic;
    private Set<String> hashCache = new HashSet<>(1000_000);
    private Map<String, String> valueToHashCache = new HashMap<>(1000_000);
    private AtomicLong counter = new AtomicLong();

    public Pipe(
            RedisRepository redisRepository,
            Flux<ReceiverRecord<String, String>> receiver,
            KafkaSender<String, String> sender) {
        this.redisRepository = redisRepository;
        this.receiver = receiver;
        this.sender = sender;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
        scheduler.scheduleWithFixedDelay(() -> LOG.info("Counter: {}", counter.get()), 1, 1, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(() -> {
            if (hashCache.size() > 1000_000) hashCache.clear();
            if (valueToHashCache.size() > 1000) valueToHashCache.clear();
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void pipe() {
        LOG.info("Pipe started");
        long start = System.currentTimeMillis();
        sender.send(receiver
                .buffer(Duration.of(100, ChronoUnit.MILLIS))
                .map(this::isUnique)
                .flatMap(buffer ->
                        Flux.fromStream(buffer.stream()
                                .map(m -> SenderRecord.create(
                                        new ProducerRecord<>(
                                                resultTopic,
                                                (String) null,
                                                getHash(m.value())),
                                        m.receiverOffset()))))
                .doOnError(e -> LOG.error("Send failed, terminating.", e))
                .doOnNext(m -> m.correlationMetadata().acknowledge())
                .doOnCancel(sender::close))
                .subscribe(msg -> {
                    if (counter.incrementAndGet() >= 380060) {
                        long finish = System.currentTimeMillis();
                        LOG.info("Pipe finished");
                        LOG.info("Length: {}. TPS: {}",
                                TimeUnit.MILLISECONDS.toSeconds(finish - start),
                                counter.get() * 1000.0 / (finish - start));
                    }
                });
    }

    private String getHash(String value) {
        return valueToHashCache.compute(value,
                (k, v) -> v == null
                        ? Hashing.murmur3_128().hashUnencodedChars(value).toString()
                        : v);
    }

    private List<ReceiverRecord<String, String>> isUnique(List<ReceiverRecord<String, String>> msgs) {
        List<ReceiverRecord<String, String>> allowed = new ArrayList<>(msgs.size());
        for (ReceiverRecord<String, String> msg : msgs) {
            String hash = getHash(msg.value());
            if (hashCache.add(hash)) {
                allowed.add(msg);
                //return redisRepository.setIfAbsent(hash, "");
            }
        }
        return allowed;
    }

}
