package com.be3c.sysmetic.global.config.security;

import com.be3c.sysmetic.domain.member.exception.MemberBadRequestException;
import com.be3c.sysmetic.domain.member.exception.MemberExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("redisTemplate0")
    private RedisTemplate<String, String> redisTemplate0; // 토큰
    @Qualifier("redisTemplate1")
    private RedisTemplate<String, String> redisTemplate1; // 이메일

    public RedisUtils(@Qualifier("redisTemplate0")RedisTemplate<String, String> redisTemplate0, @Qualifier("redisTemplate1")RedisTemplate<String, String> redisTemplate1) {
        this.redisTemplate0 = redisTemplate0;
        this.redisTemplate1 = redisTemplate1;
    }

    // [토큰]

    // 1. Redis에 토큰 저장 메서드
    public void saveToken(String accessToken, String refreshToken) {
        redisTemplate0.opsForValue().set("accessToken:" + accessToken, refreshToken);
    }

    // 2. Redis에서 토큰 조회 메서드
    public String getRefreshToken(String accessToken) {
        return redisTemplate0.opsForValue().get("accessToken:" + accessToken);
    }

    // 3. Redis에 저장된 토큰 삭제 메서드
    public void deleteToken(String accessToken) {
        redisTemplate0.delete("accessToken:" + accessToken);
    }



    // [이메일 인증코드]

    // 1. 인증코드 발송 내역 저장 메서드 (1시간 후 자동 만료 - redisUtil.setDataExpire(email, authCode, 만료시간: 1hour);)
    public void saveEmailAuthCodeWithExpireTime(String email, String authCode, Long expireTime) {
        redisTemplate1.opsForValue().set(email, authCode, expireTime, TimeUnit.MILLISECONDS);
    }

    // 2. 인증코드 조회 메서드
    public String getEmailAuthCode(String email) {
        if(redisTemplate1.opsForValue().get(email) == null) {
            // 인증코드 조회 실패 시 예외발생
            throw new MemberBadRequestException(MemberExceptionMessage.ERROR_REDIS_EMAIL_CODE.getMessage());
        }
        return redisTemplate1.opsForValue().get(email);
    }

    // 3. 인증코드 삭제 메서드 (인증 완료 시)
    public void deleteEmailAuthCode(String email) {
        redisTemplate1.delete(email);
    }

    // 0. 인증내역 저장 메서드 (1시간 후 자동 만료)
    public void saveEmailVerifyLogExpireTime(String email, String inputAuthCode, Long expireTime) {
        redisTemplate1.opsForValue().set(email, inputAuthCode, expireTime, TimeUnit.MILLISECONDS);
    }
}
