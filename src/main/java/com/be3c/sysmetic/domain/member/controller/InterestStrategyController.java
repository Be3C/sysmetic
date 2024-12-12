package com.be3c.sysmetic.domain.member.controller;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.service.InterestStrategyService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.common.response.SuccessCode;
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
public class InterestStrategyController implements InterestStrategyControllerDocs {

    private final InterestStrategyService interestStrategyService;

    /*
        폴더 내 관심 전략을 페이지로 가져오는 api
        1. 해당 관심 전략 페이지를 찾는 데 성공했을 때 : OK
        2. 해당 페이지에 관심 전략이 존재하지 않을 때 : NOT_FOUND
        3. SecurityContext에 userId가 존재하지 않을 때 : FORBIDDEN
     */
    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/member/interestStrategy")
    public ResponseEntity<APIResponse<PageResponse<InterestStrategyGetResponseDto>>> getFolderPage(
            @ModelAttribute InterestStrategyGetRequestDto interestStrategyGetRequestDto
    ) {
        try {
            PageResponse<InterestStrategyGetResponseDto> interestStrategyPage =
                    interestStrategyService.getInterestStrategyPage(
                            interestStrategyGetRequestDto
                    );

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(interestStrategyPage));
        } catch (EntityNotFoundException |
                 NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
        관심 전략 등록 api
        1. 관심 전략 등록에 성공했을 때 : OK
        2. 관심 전략 등록에 실패했을 떄 : INTERNAL_SERVER_ERROR
        3. 이미 관심 전략에 등록된 전략일 때 : BAD_REQUEST
        4. 등록할 전략을 찾지 못했을 때 : NOT_FOUND
        5. SecurityContext에 userId가 존재하지 않을 떄 : FORBIDDEN
     */
    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/strategy/follow")
    public ResponseEntity<APIResponse<String>> follow(
        @Valid @RequestBody FollowPostRequestDto followPostRequestDto
    ) {
        try {
            if(interestStrategyService.follow(followPostRequestDto)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
        관심 전략이 속한 폴더 이동
        1. 폴더 이동 성공했을 때 : OK
        2. 폴더 이동 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 옮기려는 폴더가 존재하지 않을 때 : NOT_FOUND
        4. SecurityContext에 userId가 존재하지 않을 떄 : FORBIDDEN
     */
    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/strategy/follow")
    public ResponseEntity<APIResponse<String>> MoveFolder(
            @Valid @RequestBody FollowPutRequestDto followPutRequestDto
    ) {
        try {
            if(interestStrategyService.moveFolder(followPutRequestDto)) {
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
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/strategy/follow/{strategyId}")
    public ResponseEntity<APIResponse<String>> unFollow(
            @PathVariable Long strategyId
    ) {
        try {
            if (interestStrategyService.unfollow(strategyId)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }

    /*
        관심 전략 선택 삭제 api
        1. 선택한 관심 전략 전부 삭제에 성공했을 때 : OK
        2. 선택한 관심 전략 중 일부만 삭제에 성공했을 때 : MULTI_STATUS
        3. SecurityContext에 userId가 존재하지 않을 떄 : FORBIDDEN
     */
    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/strategy/followlist")
    public ResponseEntity<APIResponse<Map<Long, String>>> unFollowList(
            @Valid @RequestBody FollowDeleteRequestDto followPostRequestDto
    ) {
        Map<Long, String> unFollowResult = interestStrategyService.unFollowList(
                followPostRequestDto
        );

        if(unFollowResult.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success());
        }

        return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                .body(APIResponse.success(SuccessCode.OK, unFollowResult));
    }
}
