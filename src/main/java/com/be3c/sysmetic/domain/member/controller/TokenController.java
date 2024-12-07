package com.be3c.sysmetic.domain.member.controller;

import com.be3c.sysmetic.domain.member.dto.TokenApiResponseDto;
import com.be3c.sysmetic.domain.member.entity.Member;
import com.be3c.sysmetic.domain.member.repository.MemberRepository;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.config.security.JwtTokenProvider;
import com.be3c.sysmetic.global.util.file.dto.FileReferenceType;
import com.be3c.sysmetic.global.util.file.dto.FileRequest;
import com.be3c.sysmetic.global.util.file.exception.FileNotFoundException;
import com.be3c.sysmetic.global.util.file.service.FileService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Tag(name = "토큰 유효성 확인 API", description = "로그인 상태 확인용")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final FileService fileService;

    @Operation(
            summary = "토큰 유효성 확인 API",
            description = "로그인 상태 확인용"
    )
    @GetMapping("/auth")
    public ResponseEntity<APIResponse<TokenApiResponseDto>> checkToken(HttpServletRequest request) {
        // Access 토큰 추출
        String accessToken = jwtTokenProvider.extractToken(request);
        if(accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(APIResponse.fail(ErrorCode.UNAUTHORIZED, "유효하지 않은 토큰이거나 토큰이 누락되었습니다."));
        }

        // 토큰에서 회원id 추출
        Claims claims = jwtTokenProvider.parseTokenClaims(accessToken);
        Long memberId = claims.get("memberId", Long.class);

        // 회원id를 통해서 Member 추출
        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);

        // roleCode("RC001"방식) 를 Role("USER"방식) 로 명칭 변환
        String role = jwtTokenProvider.roleCodeChangeRole(member.getRoleCode());

        // 프로필 이미지 경로 추출
        String profileImage = null;
        try {
            profileImage = fileService.getFilePath(new FileRequest(FileReferenceType.MEMBER, memberId));
        } catch (FileNotFoundException e) {
            log.info("프로필 이미지 추출 실패");
        }

        // 응답 객체에 회원 정보 넣기 (memberId, roleCode, email, name, nickname, phoneNumber, profileImage)
        TokenApiResponseDto dto = TokenApiResponseDto.builder()
                .memberId(memberId)
                .roleCode(role)
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .phoneNumber(member.getPhoneNumber())
                .profileImage(profileImage)
                .totalFollowerCount(member.getTotalFollow())
                .totalStrategyCount(member.getTotalStrategyCount())
                .receiveInfoConsent(Boolean.valueOf(member.getReceiveInfoConsent()))
                .receiveMarketingConsent(Boolean.valueOf(member.getReceiveMarketingConsent()))
                .build();

        // 회원 정보 반환
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(dto));
    }

}
