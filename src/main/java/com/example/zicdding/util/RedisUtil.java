package com.example.zicdding.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> redisBlackListTemplate;

    public void save(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public void setBlackList(String key, Object o, int minutes) {
        redisBlackListTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(o.getClass()));
        redisBlackListTemplate.opsForValue().set(key, o, minutes, TimeUnit.MINUTES);
    }
    public void getBlackList(String key){
        redisBlackListTemplate.opsForValue().get(key);
    }
    public boolean deleteBlackList(String key){
        return Boolean.TRUE.equals(redisBlackListTemplate.delete(key));
    }
    public boolean hashKeyBlackList(String key){
        return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(key));
    }
}
