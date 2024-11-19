package com.be3c.sysmetic.domain.strategy.controller;

import com.be3c.sysmetic.domain.strategy.dto.MethodGetResponseDto;
import com.be3c.sysmetic.domain.strategy.dto.MethodPostRequestDto;
import com.be3c.sysmetic.domain.strategy.dto.MethodPutRequestDto;
import com.be3c.sysmetic.domain.strategy.service.MethodService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MethodController {

    private final MethodService methodService;

    /*
        매매 방식 조회 (개별)
        매매 방식 조회 (페이지)
        매매 방식 아이콘 조회
        매매 방식 등록
        매매 방식 수정
        매매 방식 삭제
     */

    /*
        매매 유형 명 중복 확인 Api
        1. 중복된 이름의 매매 유형이 존재하지 않을 때 : OK
        2. 중복된 이름의 매매 유형이 존재할 때 : CONFLICT
     */
    @GetMapping("/admin/method/availability")
    public ResponseEntity<APIResponse<String>> getCheckDupl(
            @NotBlank @RequestParam String name
    ) throws Exception {
        if(methodService.duplCheck(name)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(APIResponse.fail(ErrorCode.DUPLICATE_RESOURCE, "중복된 이름입니다."));
    }

    /*
        매매 유형 찾기 Api
        1. 매매 유형 찾기 성공했을 때 : OK
        2. 매매 유형 찾기 실패했을 때 : NOT_FOUND
     */
//    @GetMapping("/admin/method/{id:[0-9]+}")
    @GetMapping("/admin/method/{id}")
    public ResponseEntity<APIResponse<MethodGetResponseDto>> getMethod(
            @NotBlank @PathVariable Long id
    ) throws Exception {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(methodService.findById(id)));
        } catch (IllegalArgumentException |
                 DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
        매매 유형 페이지로 찾기 api
        1. 매매 유형 페이지 찾기 성공했을 떄 : OK
        2. 페이지에 아무런 데이터도 존재하지 않을 때 : NOT_FOUND
        3. 잘못된 데이터가 입력됐을 때 : BAD_REQUEST
     */
    @GetMapping("/admin/methodlist")
    public ResponseEntity<APIResponse<PageResponse<MethodGetResponseDto>>> getMethods(
            @NotBlank @RequestParam Integer page
    ) throws Exception {
        try {
            PageResponse<MethodGetResponseDto> methodList = methodService.findMethodPage(page);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(methodList));
        } catch (IllegalArgumentException |
                 DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (EntityNotFoundException |
                NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
        매매 유형 등록 api
        1. 매매 유형 등록에 성공했을 때 : OK
        2. 매매 유형 등록에 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 중복 체크를 진행하지 않은 요청일 때 : BAD_REQUEST
        4. 중복된 매매 유형명이 존재할 때 : CONFLICT
     */
    @PostMapping("/admin/method")
    public ResponseEntity<APIResponse<String>> postMethod(
            @Valid @RequestBody MethodPostRequestDto methodPostRequestDto
    ) throws Exception {
        try {
            if(methodService.insertMethod(methodPostRequestDto)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (IllegalArgumentException |
                 IllegalStateException |
                 DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(APIResponse.fail(ErrorCode.DUPLICATE_RESOURCE));
        }
    }

    /*
        매매 유형 수정 메서드
        1. 매매 유형 수정에 성공했을 때 : OK
        2. 매매 유형 수정에 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 중복 체크를 진행하지 않은 요청일 때 : BAD_REQUEST
        4. 수정하려는 매매 유형이 존재하지 않을 때 : NOT_FOUND
        5. 동일한 매매 유형명이 존재할 때 : CONFLICT
     */
    @PutMapping("/admin/method")
    public ResponseEntity<APIResponse<String>> putMethod(
            @Valid @RequestBody MethodPutRequestDto methodPutRequestDto
    ) throws Exception {
        try {
            if(methodService.updateMethod(methodPutRequestDto)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (IllegalArgumentException |
                 IllegalStateException |
                 DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(APIResponse.fail(ErrorCode.DUPLICATE_RESOURCE));
        }
    }

    /*
        매매 유형 삭제 메서드
        1. 매매 유형 삭제에 성공했을 때 : OK
        2. 매매 유형 삭제에 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 삭제하려는 매매 유형을 찾지 못했을 때 : NOT_FOUND
     */
//    @DeleteMapping("/admin/method/{id:[0-9]+}")
    @DeleteMapping("/admin/method/{id}")
    public ResponseEntity<APIResponse<String>> deleteMethod(
            @NotBlank @PathVariable Long id
    ) throws Exception {
        try {
            if(methodService.deleteMethod(id)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (IllegalArgumentException |
                 DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }
}
