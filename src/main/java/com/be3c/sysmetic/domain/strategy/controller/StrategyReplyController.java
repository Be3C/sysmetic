package com.be3c.sysmetic.domain.strategy.controller;

import com.be3c.sysmetic.domain.strategy.dto.PageReplyResponseDto;
import com.be3c.sysmetic.domain.strategy.dto.ReplyDeleteRequestDto;
import com.be3c.sysmetic.domain.strategy.dto.ReplyPostRequestDto;
import com.be3c.sysmetic.domain.strategy.service.ReplyService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@Slf4j
@RequestMapping("/v1")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StrategyReplyController implements StrategyReplyControllerDocs {

    private final ReplyService replyService;

    // 나중에 mypageController로 이동해야함.
    // 개발하지 않는다고 함
//    @GetMapping("/mypage/reply/{page}")
//    public ResponseEntity<APIResponse<PageResponse<PageReplyResponseDto>>> getReplyPage(
//            @PathVariable Integer page
//    ) throws Exception {
//        try {
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(APIResponse.success(replyService.getMyReplyPage(page)));
//        } catch (AuthenticationCredentialsNotFoundException |
//                 UsernameNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(APIResponse.fail(ErrorCode.FORBIDDEN));
//        }
//    }

    /*
        댓글 페이징 api
     */
    @Override
    @PreAuthorize("permitAll()")
    @GetMapping("/strategy/{strategyId}/replies")
    public ResponseEntity<APIResponse<PageResponse<PageReplyResponseDto>>> getReplyPage(
            @PathVariable Long strategyId,
            @RequestParam Integer page
    ) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(replyService.getReplyPage(strategyId, page)));
        } catch (EntityNotFoundException |
                 NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
        댓글 등록 api
     */
    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/strategy/reply")
    public ResponseEntity<APIResponse<String>> postReply(
            @RequestBody ReplyPostRequestDto replyPostRequestDto
    ) {
        try {
            if(replyService.insertReply(replyPostRequestDto)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (AuthenticationCredentialsNotFoundException |
                UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(APIResponse.fail(ErrorCode.FORBIDDEN));
        }
    }

    // @PreAuthorize("hasRole='ROLE_USER and !ROLE_TRADER'")
    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/strategy/reply")
    public ResponseEntity<APIResponse<String>> deleteReply(
            @RequestBody ReplyDeleteRequestDto replyDeleteRequestDto
    ) {
        try {
             if(replyService.deleteReply(replyDeleteRequestDto)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (AuthenticationCredentialsNotFoundException |
                 UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(APIResponse.fail(ErrorCode.FORBIDDEN));
        }
    }
}
