package com.be3c.sysmetic.domain.member.controller;

import com.be3c.sysmetic.domain.member.dto.EmailResponseDto;
import com.be3c.sysmetic.domain.member.dto.RegisterRequestDto;
import com.be3c.sysmetic.domain.member.service.RegisterService;
import com.be3c.sysmetic.domain.member.validation.RegisterValidator;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "회원가입 API", description = "회원가입")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class RegisterController {

    private final RegisterService registerService;
    private final MessageSource messageSource;

//    private final RegisterValidator registerValidator;  // custom validator

//    @InitBinder
//    public void initBinder(WebDataBinder binder) {
//        if(binder.getTarget() instanceof RegisterRequestDto) {
//            binder.addValidators(registerValidator);    // custom Validator 등록
//        }
//    }

    // 이메일 중복확인 api
    @Operation(
            summary = "이메일 중복확인",
            description = "이메일 중복 여부를 확인하는 API"
    )
    @PreAuthorize("permitAll()")
    @GetMapping("/auth/check-duplicate-email")
    public ResponseEntity<APIResponse<String>> checkDuplicateEmail(@Email(message = "유효한 이메일 형식이 아닙니다.") @RequestParam String email) {
        registerService.checkEmailDuplication(email);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success());
    }

    // 이메일 인증코드 전송 api
    @Operation(
            summary = "이메일 인증코드 전송",
            description = "사용자에게 이메일 인증코드를 전송하는 API"
    )
    @PreAuthorize("permitAll()")
    @GetMapping("/auth/email-code")
    public ResponseEntity<APIResponse<String>> sendVerificationCode(@Email(message = "유효한 이메일 형식이 아닙니다.") @RequestParam String email) {
        registerService.sendVerifyEmailCode(email);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success());
    }

    // 이메일 인증코드 확인 api
    @Operation(
            summary = "이메일 인증코드 확인",
            description = "사용자가 입력한 이메일 인증코드를 검증하는 API"
    )
    @PreAuthorize("permitAll()")
    @PostMapping("/auth/email-code")
    public ResponseEntity<APIResponse<String>> verifyCode(@Valid @RequestBody EmailResponseDto emailResponseDto) {
        registerService.checkVerifyEmailCode(emailResponseDto.getEmail(), emailResponseDto.getAuthCode());
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success());
    }

    // 닉네임 중복확인 api
    @Operation(
            summary = "닉네임 중복확인",
            description = "닉네임 중복 여부를 확인하는 API"
    )
    @PreAuthorize("permitAll()")
    @GetMapping("/auth/check-nickname")
    public ResponseEntity<APIResponse<String>> checkDuplicateNickname(@RequestParam @Pattern(regexp = "^[가-힣0-9]{3,10}$", message = "닉네임은 한글 또는 숫자로 3자 이상 10자 이내로 입력해야 합니다.") String nickname) {
        registerService.checkNicknameDuplication(nickname);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success());
    }

    // 회원가입 api
    @Operation(
            summary = "회원가입",
            description = "사용자 회원가입을 처리하는 API"
    )
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/auth/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> register(@Valid @RequestPart RegisterRequestDto registerResponseDto,
                                                        @RequestPart(value = "file", required = false) MultipartFile file,
                                                        Errors errors
    ) {
        // RegisterValidator를 수동으로 호출
        RegisterValidator registerValidator = new RegisterValidator(messageSource);
        registerValidator.validate(registerResponseDto, errors);

        // Validation 에러가 있는 경우 예외 처리
        if (errors.hasErrors()) {
            String errorMessage = errors.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.fail(ErrorCode.BAD_REQUEST, errorMessage));
        }

        registerService.registerMember(registerResponseDto, file);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success());
    }
}