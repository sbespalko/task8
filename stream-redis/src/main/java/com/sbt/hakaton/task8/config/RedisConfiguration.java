package com.sbt.hakaton.task8.config;

import com.sbt.hakaton.task8.db.RedisRepository;
import com.sbt.hakaton.task8.db.RedisRepositoryImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
//@EnableAutoConfiguration
public class RedisConfiguration {
    @Bean
    @Primary
    public LettuceConnectionFactory redisConnectionFactory1() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration());
    }

    @Bean("R1")
    public RedisTemplate<String, String> redisTemplate1() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory1());
        template.setKeySerializer(new GenericToStringSerializer<>(String.class));
        template.setValueSerializer(new GenericToStringSerializer<>(String.class, StandardCharsets.UTF_8));
        return template;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory2() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6380));
    }

    @Bean("R2")
    public RedisTemplate<String, String> redisTemplate2() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory2());
        template.setKeySerializer(new GenericToStringSerializer<>(String.class));
        template.setValueSerializer(new GenericToStringSerializer<>(String.class, StandardCharsets.UTF_8));
        return template;
    }

    @Bean
    public RedisRepository redisRepository(
            @Qualifier("R1") RedisTemplate<String, String> redisTemplate1,
            @Qualifier("R2") RedisTemplate<String, String> redisTemplate2) {
        List<RedisTemplate<String, String>> redisTemplates = new ArrayList<>(2);
        redisTemplates.add(redisTemplate1);
        redisTemplates.add(redisTemplate2);
        return new RedisRepositoryImpl(redisTemplates);
    }
}
