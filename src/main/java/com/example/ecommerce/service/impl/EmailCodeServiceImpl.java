package com.example.ecommerce.service.impl;

import com.example.ecommerce.service.EmailCodeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailCodeServiceImpl implements EmailCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate stringRedisTemplate;

    private final Map<String, String> localCodeCache = new ConcurrentHashMap<>();

    public EmailCodeServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String sendCode(String email, String scene) {
        String code = String.valueOf(100000 + RANDOM.nextInt(900000));
        String key = buildKey(email, scene);
        try {
            stringRedisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
        } catch (Exception ex) {
            localCodeCache.put(key, code);
        }
        return code;
    }

    @Override
    public void verifyCode(String email, String scene, String code) {
        String key = buildKey(email, scene);
        String cachedCode = null;
        try {
            cachedCode = stringRedisTemplate.opsForValue().get(key);
        } catch (Exception ex) {
            cachedCode = localCodeCache.get(key);
        }
        if (!StringUtils.hasText(cachedCode) || !cachedCode.equals(code)) {
            throw new IllegalArgumentException("邮箱验证码错误或已过期");
        }
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception ex) {
            localCodeCache.remove(key);
        }
    }

    private String buildKey(String email, String scene) {
        String codeScene = StringUtils.hasText(scene) ? scene : "default";
        return "auth:email-code:" + codeScene + ":" + email;
    }
}
