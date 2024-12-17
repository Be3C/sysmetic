package com.be3c.sysmetic.domain.member.controller;

import com.be3c.sysmetic.domain.member.dto.FolderListResponseDto;
import com.be3c.sysmetic.domain.member.dto.FolderPostRequestDto;
import com.be3c.sysmetic.domain.member.dto.FolderPutRequestDto;
import com.be3c.sysmetic.domain.member.exception.ResourceLimitExceededException;
import com.be3c.sysmetic.domain.member.service.FolderService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.exception.ConflictException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/v1")
public class FolderController implements FolderControllerDocs {

    private final FolderService folderService;

    /*
        폴더명 중복 체크
        1. 중복된 폴더명이 없을 떄 : OK
        2. 중복된 폴더명이 존재할 때 : CONFLICT
     */
    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/member/folder/availability")
    public ResponseEntity<APIResponse<String>> getDuplCheck(
            @RequestParam String folderName
    ) {
        if(folderService.duplCheck(folderName)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(APIResponse.fail(ErrorCode.DUPLICATE_RESOURCE));
    }

    /*
        해당 유저의 폴더 목록을 반환하는 api
        1. 해당 유저의 폴더를 찾았다면 : OK
        2. 해당 유저의 폴더 개수가 0개라면 : NOT_FOUND
        3. SecurityContext에 userId가 존재하지 않을 떄 : FORBIDDEN
     */
    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/member/folder")
    public ResponseEntity<APIResponse<List<FolderListResponseDto>>> getAllFolder(
    ) {
        try {
            List<FolderListResponseDto> folderList = folderService.getUserFolders();

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(folderList));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
        폴더 추가 메서드
        1. 폴더 추가에 성공했을 떄 : OK
        2. 폴더 추가에 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 중복체크가 되지 않은 요청일 떄 : BAD_REQUEST
        4. 중복된 이름의 폴더가 존재할 때 : CONFLICT
        5. 현재 폴더 개수가 5개일 때 : TOO_MANY_REQUESTS
        6. SecurityContext에 userId가 존재하지 않을 떄 : FORBIDDEN
     */
    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/member/folder")
    public ResponseEntity<APIResponse<String>> postFolder(
            @Valid @RequestBody FolderPostRequestDto folderPostRequestDto
    ) {
        try {
            if(folderService.insertFolder(folderPostRequestDto)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (IllegalArgumentException |
                 IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch(ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(APIResponse.fail(ErrorCode.DUPLICATE_RESOURCE));
        } catch(ResourceLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(APIResponse.fail(ErrorCode.RESOURCE_LIMIT));
        }
    }

    /*
        폴더명 수정 메서드
        1. 폴더 수정에 성공했을 때 : OK
        2. 폴더 수정에 실패했을 떄 : INTERNAL_SERVER_ERROR
        3. 중복 체크가 진행되지 않은 요청일 때 : BAD_REQUEST
        4. 수정하려는 폴더를 찾지 못했을 때 : NOT_FOUND
        5. 중복된 이름의 폴더가 존재할 때 : CONFLICT
        6. SecurityContext에 userId가 존재하지 않을 떄 : FORBIDDEN
     */
    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/member/folder")
    public ResponseEntity<APIResponse<String>> putFolder(
            @Valid @RequestBody FolderPutRequestDto folderPutRequestDto
    ) {
        try {
            if(folderService.updateFolder(folderPutRequestDto)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch(IllegalArgumentException |
                IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
        폴더 삭제 api
        1. 폴더 삭제에 성공횄을 때 : OK
        2. 폴더 삭제에 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 현재 폴더 개수가 1개 이하일 때 : UNPROCESSABLE_ENTITY
        4. 삭제하려는 폴더를 찾지 못했을 때 : NOT_FOUND
        5. SecurityContext에 userId가 존재하지 않을 떄 : FORBIDDEN
     */
    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/member/folder/{id}")
    public ResponseEntity<APIResponse<String>> deleteFolder(
            @PathVariable Long id
    ) {
        try {
            if(folderService.deleteFolder(id)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(APIResponse.fail(ErrorCode.UNPROCESSABLE_ENTITY));
        }
    }
}
