package com.sbt.hakaton.task8.config;

import com.sbt.hakaton.task8.db.RedisRepository;
import com.sbt.hakaton.task8.db.RedisRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import java.nio.charset.StandardCharsets;

@Configuration
@ComponentScan(basePackageClasses = RedisRepository.class)
public class RedisConfiguration {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration());
    }

    @Bean
    public RedisTemplate<byte[], String> redisTemplate() {
        RedisTemplate<byte[], String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new GenericToStringSerializer<>(byte[].class));
        template.setValueSerializer(new GenericToStringSerializer<>(String.class, StandardCharsets.UTF_8));
        return template;
    }

    @Bean
    public RedisRepository repository() {
        return new RedisRepositoryImpl(redisTemplate());
    }
}
