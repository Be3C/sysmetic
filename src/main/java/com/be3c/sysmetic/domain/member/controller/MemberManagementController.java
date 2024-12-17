package com.be3c.sysmetic.domain.member.controller;

import com.be3c.sysmetic.domain.member.dto.MemberGetResponseDto;
import com.be3c.sysmetic.domain.member.dto.MemberManagementPatchRequestDto;
import com.be3c.sysmetic.domain.member.entity.MemberSearchRole;
import com.be3c.sysmetic.domain.member.entity.MemberSearchType;
import com.be3c.sysmetic.domain.member.service.MemberInfoService;
import com.be3c.sysmetic.domain.member.service.MemberManagementService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "회원관리 API", description = "관리자의 회원관리")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/v1")
public class MemberManagementController {

    private final MemberManagementService memberManagementService;
    private final MemberInfoService memberInfoService;

    // 관리자 회원 조회 api
    @Operation(
            summary = "관리자 - 회원 조회",
            description = "관리자가 특정 역할(role)과 검색 조건을 기반으로 회원 목록을 조회하는 API"
    )
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/members")
    public ResponseEntity<APIResponse<PageResponse<MemberGetResponseDto>>> getMemberPage(@RequestParam(defaultValue = "ALL") MemberSearchRole role,
                                                                                         @RequestParam(defaultValue = "0") @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.") Integer page,
                                                                                         @RequestParam(defaultValue = "ALL") MemberSearchType searchType,
                                                                                         @RequestParam(required = false) String searchKeyword) {
        PageResponse<MemberGetResponseDto> members = memberManagementService.findMemberPage(role, page, searchType, searchKeyword);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(members));
    }

    // 관리자 회원 등급변경 api
    @Operation(
            summary = "관리자 - 회원 등급변경",
            description = "관리자가 특정 회원의 등급을 변경하는 API"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/admin/members")
    public ResponseEntity<APIResponse<String>> changeMemberRoleCode(@Valid @RequestBody MemberManagementPatchRequestDto responseDto) {
        for(Long memberId : responseDto.getMemberId()) {
            memberManagementService.changeRoleCode(memberId, responseDto.getHasManagerRights());
        }
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success());
    }

    // 관리자 회원 강제탈퇴 api
    @Operation(
            summary = "관리자 - 회원 강제탈퇴",
            description = "관리자가 특정 회원을 강제 탈퇴시키는 API"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin/members/{memberId}")
    public ResponseEntity<APIResponse<String>> banMember(@PathVariable List<Long> memberId) {
        for(Long member : memberId) {
            memberInfoService.banUser(member);
        }
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success());
    }


}
