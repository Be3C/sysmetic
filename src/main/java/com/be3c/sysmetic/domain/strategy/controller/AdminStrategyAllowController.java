package com.be3c.sysmetic.domain.strategy.controller;

import com.be3c.sysmetic.domain.strategy.dto.AdminStrategyGetResponseDto;
import com.be3c.sysmetic.domain.strategy.dto.AdminStrategySearchGetDto;
import com.be3c.sysmetic.domain.strategy.dto.AllowApprovalRequestDto;
import com.be3c.sysmetic.domain.strategy.dto.RejectStrategyApprovalDto;
import com.be3c.sysmetic.domain.strategy.service.AdminStrategyService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.common.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/v1")
public class AdminStrategyAllowController implements AdminStrategyAllowControllerDocs {

    private final AdminStrategyService adminStrategyService;

    /*
        관리자 전략 관리 페이지 api
        1. 데이터를 찾는 데 성공했을 때 : OK
        2. 페이지 내에 한 개의 전략도 존재하지 않을 때 : NOT_FOUND
     */
    @Override
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/strategy")
    public ResponseEntity<APIResponse<PageResponse<AdminStrategyGetResponseDto>>> getAdminStrategy(
            @ModelAttribute AdminStrategySearchGetDto adminStrategySearchGetDto
    ) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(adminStrategyService.findStrategyPage(adminStrategySearchGetDto)));
        } catch (NoSuchElementException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
        선택(단일 포함) 전략 승인 api
        1. 요청한 모든 전략 승인에 성공했을 때 : OK
        2. 요청한 전략 중 승인에 실패했을 때 : MULTI_STATUS
     */
    @Override
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @PatchMapping("/admin/strategy/allow")
    public ResponseEntity<APIResponse<Map<Long, String>>> strategyAllow(
            @Valid @RequestBody AllowApprovalRequestDto allowApprovalRequestDto
    ) {
        Map<Long, String> resultMap = adminStrategyService.StrategyApproveApplyAllow(allowApprovalRequestDto);

        if(resultMap.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(SuccessCode.OK, null));
        }

        return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                .body(APIResponse.success(SuccessCode.OK, resultMap));
    }

    /*
        단일 전략 공개 요청 거절 api
        1. 전략 공게 요청 거절에 성공했을 때 : OK
        2. 전략 공개 요청 거절에 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 해당 전략의 공개 요청을 찾지 못했을 때 : NOT_FOUND
     */
    @Override
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @PatchMapping("/admin/strategy/reject")
    public ResponseEntity<APIResponse<String>> strategyReject(
            @Valid @RequestBody RejectStrategyApprovalDto rejectStrategyApprovalDto
    ) {
        try {
            if(adminStrategyService.rejectStrategyApproval(rejectStrategyApprovalDto)) {
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
