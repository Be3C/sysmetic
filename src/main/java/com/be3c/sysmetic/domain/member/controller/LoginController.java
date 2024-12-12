package com.be3c.sysmetic.domain.member.controller;

import com.be3c.sysmetic.domain.member.dto.LoginRequestDto;
import com.be3c.sysmetic.domain.member.service.LoginService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.config.security.RedisUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@Tag(name = "로그인 API", description = "로그인")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class LoginController {
    /*
        [순서]
        1. email, pw 형식 체크
            1-1. 형식 불일치 -> 실패(Error)
            1-1. 형식 일치 -> 2.으로 이동
        2. DB에서 email 조회
            2-1. 존재X -> 실패(Error)
            2-2. 존재O -> 3.으로 이동
        3. DB에서 해당 email의 pw와 비교
            3-1. 일치X -> 실패(Error)
            3-2. 일치O -> 4.으로 이동
        4. rememberMe 여부에 따른 jwt 토큰 생성
            4-1. 체크박스 선택O -> refresh토큰 30일로 발급
            4-2. 체크박스 선택X -> refresh토큰 1시간으로 발급
        5. jwt 전달
            5-1. access 토큰은 response에 담아서 클라이언트에게 전달
            5.2. refresh 토큰은 Redis에 저장
     */

    private final LoginService loginService;
    private final RedisUtils redisUtils;

    // 로그인 api
    @Operation(
            summary = "로그인",
            description = "사용자가 이메일과 비밀번호를 통해 로그인하는 API"
    )
    @PreAuthorize("permitAll()")
    @PostMapping("/auth/login")
    public ResponseEntity<APIResponse<LoginRequestDto>> login(@RequestBody @Valid LoginRequestDto requestDto,
                                                              HttpServletResponse response) {

        // 이메일 확인
        String memberEmail = loginService.findEmail(requestDto.getEmail());

        // 비밀번호 비교
        loginService.validatePassword(requestDto.getEmail(), requestDto.getPassword());

        // rememberMe에 따른 Jwt 토큰 생성
        Map<String,String> tokenMap = loginService.generateTokenBasedOnRememberMe(memberEmail,requestDto.getRememberMe());

        // 응답 헤더에 토큰 추가
        response.setHeader("Authorization", "Bearer " + tokenMap.get("accessToken"));

        // 생성된 토큰 Redis에 저장
        redisUtils.saveToken(tokenMap.get("accessToken"), tokenMap.get("refreshToken"));

        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success());
    }

}
