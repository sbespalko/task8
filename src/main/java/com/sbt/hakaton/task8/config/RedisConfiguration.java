package com.sbt.hakaton.task8.config;

import com.sbt.hakaton.task8.db.RedisRepository;
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
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new GenericToStringSerializer<>(String.class, StandardCharsets.UTF_8));
        template.setValueSerializer(new GenericToStringSerializer<>(String.class, StandardCharsets.UTF_8));
        return template;
    }
}
