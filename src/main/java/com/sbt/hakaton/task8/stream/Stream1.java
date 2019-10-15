package com.sbt.hakaton.task8.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Stream1 {

    @Value("${topic.initial}")
    private String initialTopic;

    @Value("${topic.result}")
    private String resultTopic;

    @Bean
    public KStream<String, String> startProcessing(StreamsBuilder builder) {

        KStream<String, String> toSquare = builder.stream(initialTopic, Consumed.with(Serdes.String(), Serdes.String()));
        toSquare.map((key, value) -> {
            return KeyValue.pair(key, value.toUpperCase());
        }).to(resultTopic, Produced.with(Serdes.String(), Serdes.String()));

        return toSquare;
    }
}
