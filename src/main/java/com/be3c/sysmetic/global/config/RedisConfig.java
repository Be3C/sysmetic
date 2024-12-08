package com.be3c.sysmetic.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.core.parameters.P;

@Slf4j
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory0() {   // RedisConnectionFactory: Redis DB와 연결하는 역할
        log.info("redisConnectionFactory 0 : 등록");
        return createRedis(0);
    }

    @Bean(name = "redisTemplate0")
    @Primary
    public RedisTemplate<String, String> redisTemplate0() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        redisTemplate.setConnectionFactory(redisConnectionFactory0());
        return redisTemplate;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory1() {
        log.info("redisConnectionFactory 1 : 등록");
        return createRedis(1);
    }

    @Bean(name = "redisTemplate1")
    public RedisTemplate<String, String> redisTemplate1() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory1());
        return redisTemplate;
    }

//    @Bean
//    public RedisConnectionFactory redisConnectionFactory1() {
//        log.info("redisConnectionFactory 1 : 등록");
//        return createRedis(1);
//    }
//
//    @Bean
//    public RedisTemplate<Long, Object> redisTemplate1() {
//        RedisTemplate<Long, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setKeySerializer(new GenericJackson2JsonRedisSerializer());
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        redisTemplate.setConnectionFactory(redisConnectionFactory1());
//        return redisTemplate;
//    }

    private RedisConnectionFactory createRedis(int index) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        // setDatabase : 데이터베이스 번호 지정
        redisStandaloneConfiguration.setDatabase(index);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }
}