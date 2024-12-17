package com.be3c.sysmetic.domain.member.controller;

import org.springframework.http.MediaType;
import com.be3c.sysmetic.domain.member.dto.MemberPatchConsentRequestDto;
import com.be3c.sysmetic.domain.member.dto.MemberPatchInfoRequestDto;
import com.be3c.sysmetic.domain.member.dto.MemberPutPasswordRequestDto;
import com.be3c.sysmetic.domain.member.service.MemberInfoService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.util.SecurityUtils;
import com.be3c.sysmetic.global.util.email.service.EmailService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/v1")
public class MemberInfoController implements MemberInfoControllerDocs {

    private final MemberInfoService memberInfoService;

    private final EmailService emailService;

    /*
        회원 비밀번호 변경 api
        1. 비밀번호 변경에 성공했을 때 : OK
        2. 비밀번호 변경에 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 해당 유저가 존재하지 않을 때 : NOT_FOUND
        4. Security Context에 userId가 존재하지 않을 때 : FORBIDDEN
     */
    @Override
    @PreAuthorize("isAuthentication()")
    @PatchMapping("/member/info/{id}/password")
    public ResponseEntity<APIResponse<String>> putPassword(
            @PathVariable Long id,
            @RequestBody MemberPutPasswordRequestDto memberPutPasswordRequestDto,
            HttpServletRequest request
    ) {
        try {
            if(memberInfoService
                    .changePassword(
                            id,
                            memberPutPasswordRequestDto,
                            request)
            ) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
        }
    }

    /*
        회원 정보를 업데이트하는 api
        1. 수정에 성공했을 때 : OK
        2. 수정에 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 수정할 회원을 찾지 못했을 때 : NOT_FOUND
        4. Security Context에서 회원 Id를 찾지 못했을 때 : FORBIDDEN
     */
    @Override
    @PreAuthorize("isAuthentication()")
    @PatchMapping(value= "/member/info/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> updateMemberInfo(
            @PathVariable Long id,
            @RequestPart MemberPatchInfoRequestDto memberPatchInfoRequestDto,
            @RequestPart(name="file", required = false) MultipartFile file
    ) {
        try {
            if(memberInfoService
                    .changeMemberInfo(
                            id,
                            memberPatchInfoRequestDto,
                            file
                    )) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    @Override
    @PreAuthorize("isAuthentication()")
    @PatchMapping("/member/consent/{id}")
    public ResponseEntity<APIResponse<String>> updateMemberConsent(
            @PathVariable Long id,
            @RequestBody MemberPatchConsentRequestDto memberPatchConsentRequestDto
    ) {
        try {
            memberInfoService.changeMemberConsent(id, memberPatchConsentRequestDto);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
            회원 탈퇴 api
            1. 회원 탈퇴에 성공했을 때 : OK
            2. 회원 탈퇴에 실패했을 때 : INTERNAL_SERVER_ERROR
            3. 탈퇴할 회원을 찾지 못했을 때 : NOT_FOUND
            4. Security Context에서 회원 아이디를 찾지 못했을 때 : FORBIDDEN
         */
    @Override
    @PreAuthorize("isAuthentication()")
    @DeleteMapping("/member/{id}")
    public ResponseEntity<APIResponse<String>> deleteMemberInfo(
            @PathVariable(name="id") Long userId
    ) {
        try {
            if(memberInfoService.deleteUser(userId)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }
}
