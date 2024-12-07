package com.be3c.sysmetic.global.util.email.service;

import com.be3c.sysmetic.global.config.security.RedisUtils;
import com.be3c.sysmetic.global.util.email.apiclient.EmailApiClient;
import com.be3c.sysmetic.global.util.email.dto.*;
import com.be3c.sysmetic.global.util.email.exception.EmailSendingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.lettuce.core.RedisConnectionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailApiClient emailApiClient;
    private final RedisUtils redisUtils;

    @Override
    public StibeeApiResponse addUserSubscriberRequest(SubscriberRequest subscriberRequest) {
        try {
            return emailApiClient.addUserSubscriberRequest(subscriberRequest);
        } catch (Exception e) {
            handleException(e, "일반회원 구독 요청 실패");
        }
        return null;
    }

    @Override
    public StibeeApiResponse addTraderSubscriberRequest(SubscriberRequest subscriberRequest) {
        try {
            return emailApiClient.addTraderSubscriberRequest(subscriberRequest);
        } catch (Exception e) {
            handleException(e, "트레이더 구독 요청 실패");
        }
        return null;
    }


    @Override
    public StibeeApiResponse updateUserSubscriberRequest(SubscriberRequest subscriberRequest) {
        try {
            subscriberRequest.setEventOccurredBy(EventOccuredBy.MANUAL);
            return emailApiClient.addUserSubscriberRequest(subscriberRequest);
        } catch (Exception e) {
            handleException(e, "일반회원 구독 수정 요청 실패");
        }
        return null;
    }

    @Override
    public StibeeApiResponse updateTraderSubscriberRequest(SubscriberRequest subscriberRequest) {
        try {
            subscriberRequest.setEventOccurredBy(EventOccuredBy.MANUAL);
            return emailApiClient.addTraderSubscriberRequest(subscriberRequest);
        } catch (Exception e) {
            handleException(e, "트레이더 구독 수정 요청 실패");
        }
        return null;
    }


    @Override
    public Mono<StibeeSimpleResponse> deleteUserSubscriberRequest(List<String> emails) {
        return emailApiClient.deleteUserSubscriberRequest(emails);
    }

    @Override
    public Mono<StibeeSimpleResponse> deleteTraderSubscriberRequest(List<String> emails) {
        return emailApiClient.deleteTraderSubscriberRequest(emails);
    }


    // 자동 email API -------------------------------------------------------------------------

    @Override
    public Mono<Void> sendAndSaveAuthCode(String toEmail) {

        String authCode = generateAuthCode();


        return Mono.fromRunnable(() -> {

                    emailApiClient.addTempSubscriberRequest(SubscriberRequest.builder()
                            .subscribers(List.of(Subscriber.builder().email(toEmail).build()))
                            .build());
                })
                .then(Mono.delay(Duration.ofSeconds(5))) // 5초 지연 : 스티비 작동 안 됨 문제로 필수
                .then(Mono.fromRunnable(() -> {

                    Mono<String> sendMonoResponse = emailApiClient.sendAuthEmailRequest(new AuthCodeRequest(toEmail, authCode));
                    redisUtils.saveEmailAuthCodeWithExpireTime(toEmail, authCode, TimeUnit.HOURS.toMillis(1));

                    sendMonoResponse.subscribe(response -> {
                        // 정상 응답값 : "ok"
                        if (!"ok".equals(response)) {
                            log.error("이메일 인증코드 발송 실패: 응답 'ok'가 아님 응답: {} 수신자: {}", response, toEmail);
                        }
                    });
                }))
                .then(Mono.delay(Duration.ofSeconds(5))) // 5초 지연
                .then(Mono.fromRunnable(() -> {

                    emailApiClient.deleteTempSubscriberRequest(List.of(toEmail));
                }));
    }

    private String generateAuthCode() {

        String authCodeCharset =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                        + "0123456789";

        // ThreadLocalRandom을 사용하여 랜덤 인덱스를 선택
        StringBuilder authCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(authCodeCharset.length());
            authCode.append(authCodeCharset.charAt(randomIndex));
        }

        return authCode.toString();
    }

    @Override
    public Mono<String> notifyStrategyInquiryRegistration(InquiryRequest inquiryRequest) {

            return emailApiClient.sendInquiryRegistrationEmailRequest(inquiryRequest);
    }

    @Override
    public Mono<String> notifyStrategyInterestRegistration(InterestRequest interestRequest) {

            return emailApiClient.sendInterestRegistrationEmailRequest(interestRequest);
    }


    /**
     * Exception 처리 공통화 메서드
     * @param e 예외 객체
     * @param message 예외 메세지
     */
    private void handleException(Exception e, String message) {
        if (e instanceof WebClientResponseException) {
            WebClientResponseException webClientException = (WebClientResponseException) e;
            log.error("{} : API 호출 실패. 상태 코드 = {}, 메시지 = {}", message,
                    webClientException.getStatusCode().value(),
                    webClientException.getResponseBodyAsString());
        } else if (e instanceof IllegalArgumentException) {
            log.error("{} : 잘못된 요청 값입니다. 메시지: {}", message, e.getMessage());
        } else if (e instanceof IOException) {
            log.error("{} : 입출력 처리 중 오류 발생. 메시지: {}", message, e.getMessage());
        } else if (e instanceof RedisConnectionException) {
            log.error("{} : Redis 연결 오류 발생. 메시지: {}", message, e.getMessage());
        } else if (e instanceof JsonProcessingException) {
            log.error("{} : JSON 직렬화 오류 발생. 메시지: {}", message, e.getMessage());
        } else {
            log.error("{} : {}", message, e.getMessage(), e);
        }
        throw new EmailSendingException(message, e);
    }


}
