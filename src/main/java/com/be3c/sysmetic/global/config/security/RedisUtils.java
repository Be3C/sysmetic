package com.be3c.sysmetic.global.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtils {
    /*
        [토큰]
        1. 토큰 저장 메서드
        2. 토큰 조회 메서드
        3. 저장된 토큰 삭제 메서드

        [이메일 인증코드]
        1. 인증코드 발송 내역 저장 메서드
        2. 인증코드 조회 메서드
        3. 인증코드 삭제 메서드 (인증 완료 시)
     */

    private final RedisTemplate<String, String> redisTemplate0;

    // [토큰]

    // 1. Redis에 토큰 저장 메서드 (성공 여부를 확인하기 위해 boolean으로 바꾸고 싶다)
    public void saveToken(String accessToken, String refreshToken) {
        redisTemplate0.opsForValue().set("refreshToken:" + accessToken, refreshToken);
    }

    // 2. Redis에서 토큰 조회 메서드
    public String getRefreshToken(String accessToken) {
        return redisTemplate0.opsForValue().get("refreshToken:" + accessToken);
    }

    // 3. Redis에 저장된 토큰 삭제 메서드 (성공 여부를 확인하기 위해 boolean으로 바꾸고 싶다)
    public void deleteToken(String accessToken) {
        redisTemplate0.delete("refreshToken:" + accessToken);
    }



    // [이메일 인증코드]

    // 1. 인증코드 발송 내역 저장 메서드 (자동 만료 - redisUtil.setDataExpire(email, authCode, 만료시간: 1hour);)
    public void saveEmailAuthCodeWithExpireTime(String email, String authCode, Long expireTime) {
        redisTemplate0.opsForValue().set(email, authCode, expireTime, TimeUnit.MILLISECONDS);
    }

    // 2. 인증코드 조회 메서드
    public String getEmailAuthCode(String email) {
        String authCode = redisTemplate0.opsForValue().get(email);
        return authCode;
    }

    // 3. 인증코드 삭제 메서드 (인증 완료 시)
    public void deleteEmailAuthCode(String email) {
        redisTemplate0.delete(email);
    }
}
