package com.be3c.sysmetic.domain.strategy.controller;

import com.be3c.sysmetic.domain.strategy.dto.StockDeleteRequestDto;
import org.springframework.http.MediaType;
import com.be3c.sysmetic.domain.strategy.dto.StockGetResponseDto;
import com.be3c.sysmetic.domain.strategy.dto.StockPostRequestDto;
import com.be3c.sysmetic.domain.strategy.dto.StockPutRequestDto;
import com.be3c.sysmetic.domain.strategy.service.StockService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/v1")
public class StockController implements StockControllerDocs {

    private final StockService stockService;

    /*
        추가 필요 사항
        1. S3에 아이콘 업로드 / DB 수정 코드 => 완료
        2. Global Exception Handler - AuthorizationException 처리 핸들러 필요.
     */

    /*
        종목명 중복 검사 메서드
        1. 동일한 종목 명이 존재하지 않을 떄 : OK
        2. 동일한 종목 명이 존재할 떄 : CONFLICT
        3. SecurityContext에 userId가 존재하지 않을 떄 : FORBIDDEN
     */
    @Override
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/stock/availability")
    public ResponseEntity<APIResponse<String>> getCheckDupl(
            @RequestParam String name
    ) {
        if(stockService.duplCheck(name)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(APIResponse.fail(ErrorCode.DUPLICATE_RESOURCE));
    }

    /*
        단일 종목 찾기
        1. 해당 아이디의 종목을 찾는 데 성공했을 때 : OK
        2. 해당 아이디의 종목을 찾는 데 실패했을 떄 : NOT_FOUND
     */
    @Override
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/stock/{id}")
    public ResponseEntity<APIResponse<StockGetResponseDto>> getItem(
            @PathVariable Long id
    ) {
        try {
            StockGetResponseDto findStock = stockService.findItemById(id);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(findStock));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
        종목 관리 - 종목 페이지 표시.
        1. 해당 페이지의 종목을 찾는 데 성공했을 때 : OK
        2. 해당 페이지에 아무런 종목이 존재하지 않을 때 : NOT_FOUND
        3. 해당 페이지의 종목을 찾는 데 실패했을 때 : INTERNAL_SERVER_ERROR
     */
    @Override
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/stocklist/{page}")
    public ResponseEntity<APIResponse<PageResponse<StockGetResponseDto>>> getStockPage(
            @PathVariable Integer page
    ) {
        try {
            PageResponse<StockGetResponseDto> stockPage = stockService.findItemPage(page);
            if(!stockPage.getContent().isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success(stockPage));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
        종목 저장하기
        1. 종목을 저장하는 데 성공했을 때 : OK
        2. 종목을 저장하는 데 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 중복 검사가 되지 않은 요청일 때 : BAD_REQUEST
        4. NOT NULL 값에 NULL이 들어왔을 때 : BAD_REQUEST
        5. 중복된 종목명이 존재할 때 : BAD_REQUEST
     */
    @Override
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/admin/stock", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> saveitem(
            @Valid @RequestPart StockPostRequestDto stockPostRequestDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            if(stockService.saveItem(stockPostRequestDto, file)) {
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
        }
    }

    /*
        종목 수정하기
        1. 종목을 수정하는 데 성공했을 때 : OK
        2. 종목을 수정하는 데 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 수정해야할 종목을 찾지 못했을 때 : NOT_FOUND
     */
    @Override
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/admin/stock", consumes = {"multipart/form-data"})
    public ResponseEntity<APIResponse<String>> updateItem(
            @Valid @RequestPart("stockPutRequestDto") StockPutRequestDto stockPutRequestDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            if(stockService.updateItem(stockPutRequestDto, file)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch(IllegalArgumentException |
                IllegalStateException |
                DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /*
        종목 삭제하기 api
        1. 종목을 삭제하는 데 성공했을 때 : OK
        2. 종목을 삭제하는 데 실패했을 때 : INTERNAL_SERVER_ERROR
        3. 삭제할 종목을 찾지 못했을 때 : NOT_FOUND
     */
    @Override
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin/stock")
    public ResponseEntity<APIResponse<Map<Long, String>>> deleteItem(
            @RequestBody StockDeleteRequestDto requestDto
    ) {
        try {
            Map<Long, String> map = stockService.deleteItem(requestDto);
            if(map.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                    .body(APIResponse.fail(ErrorCode.MULTI_STATUS));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }
}
