package com.sbt.hakaton.task8.stream;

import com.sbt.hakaton.task8.db.RedisRepository;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Stream1 {

    private final RedisRepository<String> redis;

    @Value("${topic.initial}")
    private String initialTopic;

    @Value("${topic.result}")
    private String resultTopic;

    public Stream1(RedisRepository<String> redis) {
        this.redis = redis;
    }

    @Bean
    public KStream<String, String> startProcessing(StreamsBuilder builder) {

        KStream<String, String> stream = builder.stream(initialTopic, Consumed.with(Serdes.String(), Serdes.String()));
        stream
                .filter(((key, value) -> redis.putIfAbsent(value)))
                .to(resultTopic, Produced.with(Serdes.String(), Serdes.String()));
        return stream;
    }
}
