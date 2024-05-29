package com.fc.sns.configuration;

import com.fc.sns.model.User;
import io.lettuce.core.RedisURI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
@EnableRedisRepositories
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisURI redisURI = RedisURI.create(redisProperties.getUrl());
        log.info(redisURI.toString());
        RedisConfiguration redisConfiguration = LettuceConnectionFactory.createRedisConfiguration(redisURI);
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisConfiguration);
        lettuceConnectionFactory.afterPropertiesSet();
        return lettuceConnectionFactory;
    }

    /**
     * RedisTemplate <br/>
     * Redis 의 Command들을 쉽게 사용할 수 있게 해주는 클래스 <br/>
     * 캐싱 대상 : User, Alarm, Post, Comment, Like 등 <br/>
     * <다섯가지 데이터 중 고려해야할 부분> <br/>
     * 1. 변경이 너무 많은 데이터는 캐싱 의미가 없음. <br/>
     *      데이터가 변경될 때 마다 캐싱해야 하므로 비용이 자주 발생 (캐싱, 셀렉트) <br/>
     * 2. 자주 사용하는 데이터를 캐싱하는 것이 좋다. <br/>
     *      자주 접근하는 데이터를 캐싱할 수록 DB 부하가 줄어들기 때문에 효과적 <br/>
     */
    @Bean
    public RedisTemplate<String, User> userRedisTemplate() {
        RedisTemplate<String, User> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer()); //데이터를 redis에 저장할 때 Java 객체를 Serialize 직렬화 하여 저장해야함.
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<User>(User.class));
        return redisTemplate;
    }
}
