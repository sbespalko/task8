package com.sbt.hakaton.task8.config;

import com.sbt.hakaton.task8.db.RedisRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@ComponentScan(basePackageClasses = RedisRepository.class)
public class RedisConfiguration {

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory(
                new RedisClusterConfiguration(),
                JedisClientConfiguration.defaultConfiguration());
    }

    @Bean
    RedisTemplate<byte[], String> redisTemplate() {
        RedisTemplate<byte[], String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        return redisTemplate;
    }
}
