package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.RegisterRequestDto;
import com.be3c.sysmetic.domain.member.entity.Folder;
import com.be3c.sysmetic.domain.member.entity.Member;
import com.be3c.sysmetic.domain.member.exception.MemberBadRequestException;
import com.be3c.sysmetic.domain.member.exception.MemberExceptionMessage;
import com.be3c.sysmetic.domain.member.repository.FolderRepository;
import com.be3c.sysmetic.domain.member.repository.MemberRepository;
import com.be3c.sysmetic.global.common.Code;
import com.be3c.sysmetic.global.config.security.RedisUtils;
import com.be3c.sysmetic.global.util.email.dto.Subscriber;
import com.be3c.sysmetic.global.util.email.dto.SubscriberRequest;
import com.be3c.sysmetic.global.util.email.service.EmailService;
import com.be3c.sysmetic.global.util.file.dto.FileRequest;
import com.be3c.sysmetic.global.util.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.be3c.sysmetic.global.util.file.dto.FileReferenceType.MEMBER;

@Slf4j
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
    private final FolderRepository folderRepository;
    private final RedisUtils redisUtils;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private final EmailService emailService;
    private final FileService fileService;

    // 1. 이메일 중복확인
    @Override
    public boolean checkEmailDuplication(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        if(member.isPresent()) {
            // 이메일 사용 불가능(중복O) -> 예외 발생
            throw new MemberBadRequestException(MemberExceptionMessage.ALREADY_USE_EMAIL.getMessage());
        }
        // 이메일 사용 가능 (중복X)
        return true;
    }

    // 2. 이메일 인증코드 발송 및 저장
    @Override
    public boolean sendVerifyEmailCode(String email) {
        try {
            emailService.sendAndSaveAuthCode(email).subscribe();
        } catch (Exception e) {
            // 이메일 관련 예외 발생 시
            throw new MemberBadRequestException(MemberExceptionMessage.ERROR_EMAIL.getMessage());
        }
        // 이메일 인증코드 발송 및 저장 성공
        return true;
    }

    // 0. 이메일 인증코드 확인
    @Override
    public boolean checkVerifyEmailCode(String email, String inputEmailCode) {
        // Redis에 저장된 인증코드 가져오기
        String savedAuthCode = redisUtils.getEmailAuthCode(email);

        // 사용자 입력 인증코드 일치 여부 확인
        if(!inputEmailCode.equals(savedAuthCode)) {
            throw new MemberBadRequestException(MemberExceptionMessage.INVALID_EMAIL_CODE.getMessage());
        }

        // 인증코드 일치한 경우, 인증 내역 저장 (1시간 동안 유효)
        redisUtils.saveEmailVerifyLogExpireTime("Verify " + email, inputEmailCode, TimeUnit.HOURS.toMillis(1));

        return true;
    }

    // 4. 닉네임 중복확인
    @Override
    public boolean checkNicknameDuplication(String nickname) {
        Optional<Member> member = memberRepository.findByNickname(nickname);
        if(member.isPresent()) {
            // 닉네임 사용 불가능(중복O) -> 예외 발생
            throw new MemberBadRequestException(MemberExceptionMessage.NICKNAME_ALREADY_IN_USE.getMessage());
        }
        // 닉네임 사용 가능(중복X)
        return true;
    }

    // 0. 회원가입 - 회원 정보 저장 메서드
    private Member saveMember(RegisterRequestDto dto) {
        try {
            Member member = Member.builder()
                    .roleCode(dto.getRoleCode().toString())
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
            return member;
        } catch (DateTimeParseException e) {
            log.error("잘못된 날짜 형식으로 인해 회원가입 실패");
            throw new MemberBadRequestException(MemberExceptionMessage.REGISTRATION_FAILED.getMessage(), e);
        } catch (DataIntegrityViolationException e) {
            log.error("데이터 무결성 위반으로 인해 회원가입 실패");
            throw new MemberBadRequestException(MemberExceptionMessage.REGISTRATION_FAILED.getMessage(), e);
        } catch (InvalidDataAccessApiUsageException e) {
            log.error("잘못된 데이터 접근으로 인해 회원가입 실패");
            throw new MemberBadRequestException(MemberExceptionMessage.REGISTRATION_FAILED.getMessage(), e);
        } catch (Exception e) {
            log.error("회원가입 중 알 수 없는 오류 발생");
            throw new MemberBadRequestException(MemberExceptionMessage.REGISTRATION_FAILED.getMessage(), e);
        }
    }

    // 0. 회원가입/비밀번호 재설정 - 이메일 인증 더블 체크 메서드
    @Override
    public void emailDoubleCheck(String email) {
        String inputAuthCode;
        String savedAuthCode;

        try {
            inputAuthCode = redisUtils.getEmailAuthCode("Verify " + email);
            savedAuthCode = redisUtils.getEmailAuthCode(email);

        // 저장된 인증번호를 찾을 수 없는 경우 예외 발생
        } catch (MemberBadRequestException e) {
            throw new MemberBadRequestException(MemberExceptionMessage.NOT_FOUND_EMAIL_CODE.getMessage());
        }

        // 인증번호가 불일치하는 경우 예외 발생
        if(!savedAuthCode.equals(inputAuthCode)) {
            throw new MemberBadRequestException(MemberExceptionMessage.NOT_MATCH_EMAIL_CODE.getMessage());
        }
    }

    // 5. 회원가입
    @Override
    @Transactional
    public boolean registerMember(RegisterRequestDto dto, MultipartFile file) {
        // 이메일 중복체크
        checkEmailDuplication(dto.getEmail());

        // 이메일 인증 더블체크
        emailDoubleCheck(dto.getEmail());

        // 닉네임 중복체크
        checkNicknameDuplication(dto.getNickname());

        // 회원정보 저장
        Member member = saveMember(dto);

        // 프로필 이미지 저장
        if(file != null && !file.isEmpty()) {
            fileService.uploadImage(file, new FileRequest(MEMBER, member.getId()));
        }

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

        // 회원가입 시 해당 회원에게 default 폴더 추가
        folderRepository.save(Folder.builder()
                        .name("default")
                        .member(member)
                        .latestInterestStrategyAddedDate(LocalDateTime.now())
                        .internalInterestStrategyCount(0)
                        .statusCode(Code.USING_STATE.getCode())
                        .build()
        );

        // 회원가입 성공
        return true;
    }

}