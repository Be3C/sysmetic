package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.RegisterRequestDto;
import com.be3c.sysmetic.domain.member.entity.Member;
import com.be3c.sysmetic.domain.member.repository.MemberRepository;
import com.be3c.sysmetic.global.config.security.RedisUtils;
import com.be3c.sysmetic.global.util.email.dto.Subscriber;
import com.be3c.sysmetic.global.util.email.dto.SubscriberRequest;
import com.be3c.sysmetic.global.util.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class RegisterServiceImpl implements RegisterService {
    /*
        [받아야 하는 데이터]
        프로필 이미지 (선택)
        회원등급
        이메일
        비밀번호
        비밀번호 재입력
        이름
        닉네임
        생년월일
        휴대폰 번호
        정보성 수신 동의 여부
        정보성 수신 동의일
        마케팅 수신 동의 여부
        마케팅 수신 동의일

        [자동 입력할 데이터 - 회원가입 트랜잭션 완료될 때 입력]
        식별 번호 - auto increase
        사용 상태 코드 - 유효
        총 팔로워 수 - 0

        [회원가입 과정]
        회원 유형 선택
        회원정보 입력
        이메일 중복확인
        닉네임 중복확인
        회원가입 신청

        [순서 및 메서드]
         0. 형식 확인 (컨트롤러)
         1. 이메일 중복확인
         2. 이메일 인증코드 발송 및 저장 (수정필요)
         3. 이메일 인증코드 확인
         4. 닉네임 중복확인
         5. 회원가입
     */
    private final MemberRepository memberRepository;
    private final RedisUtils redisUtils;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private final EmailService emailService;

    // 1. 이메일 중복확인
    @Override
    public boolean checkEmailDuplication(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        if(member.isPresent()) {
            return true; // 이메일 중복O
        }
        return false; // 이메일 중복X
    }

    // 2. 이메일 인증코드 발송 및 저장
    @Override
    @Transactional
    public boolean sendVerifyEmailCode(String email) {

        emailService.sendAndSaveAuthCode(email).subscribe();
        return true;
    }

    // 3. 이메일 인증코드 확인
    @Override
    public boolean checkVerifyEmailCode(String email, String inputEmailCode) {
        String savedAuthCode = redisUtils.getEmailAuthCode(email);
        System.out.println("savedAuthCode = " + savedAuthCode);
        if(!inputEmailCode.equals(savedAuthCode)) {
            return false; // 인증 실패
        }

        // 인증 성공하면 인증내역 지우기
        redisUtils.deleteEmailAuthCode(email);
        return true; // 인증 성공
    }

    // 4. 닉네임 중복확인
    @Override
    public boolean checkNicknameDuplication(String nickname) {
        Optional<Member> member = memberRepository.findByNickname(nickname);
        if(member.isPresent()) {
            return false; // 중복X
        }
        return true; // 중복O
    }

    // 5. 회원가입
    @Override
    @Transactional
    public boolean registerMember(RegisterRequestDto dto) {
        try {
            Member member = new Member().toBuilder()
                    .roleCode(dto.getRoleCode().getCode())
                    .email(dto.getEmail())
                    .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                    .name(dto.getName())
                    .nickname(dto.getNickname())
                    .birth(LocalDate.parse(dto.getBirth()))
                    .phoneNumber(dto.getPhoneNumber())
                    .receiveInfoConsent(String.valueOf(dto.getReceiveInfoConsent()))
                    .infoConsentDate(LocalDateTime.parse(dto.getInfoConsentDate()))
                    .receiveMarketingConsent(String.valueOf(dto.getReceiveMarketingConsent()))
                    .marketingConsentDate(LocalDateTime.parse(dto.getMarketingConsentDate()))
                    .build();
            memberRepository.save(member);

            // 메일링 서비스에 등록하고 가입 이메일 발송
            SubscriberRequest subscriberRequest = SubscriberRequest.builder()
                    .subscribers(List.of(
                            Subscriber.builder()
                                    .email(dto.getEmail())
                                    .name(dto.getNickname())
                                    .subscribedDate(LocalDateTime.now())
                                    .isAdConsent(true)
                                    .build()
                    ))
                    .build();

            switch (dto.getRoleCode()) {
                case USER:
                    emailService.addUserSubscriberRequest(subscriberRequest);
                    break;
                case TRADER:
                    emailService.addTraderSubscriberRequest(subscriberRequest);
                    break;
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("회원가입에 실패하였습니다.", e);
        }
    }

}