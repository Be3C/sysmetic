package com.be3c.sysmetic.domain.member.controller;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.InquiryClosed;
import com.be3c.sysmetic.domain.member.entity.InquirySearchType;
import com.be3c.sysmetic.domain.member.entity.InquirySort;
import com.be3c.sysmetic.domain.member.service.InquiryAnswerService;
import com.be3c.sysmetic.domain.member.service.InquiryService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.common.response.SuccessCode;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/v1")
public class InquiryController implements InquiryControllerDocs {

    private final InquiryService inquiryService;
    private final InquiryAnswerService inquiryAnswerService;

    /*
        관리자 문의 조회 / 검색 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의 데이터 조회에 성공했을 때 : OK
        3. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/qna")
    public ResponseEntity<APIResponse<PageResponse<InquiryAdminListOneShowResponseDto>>> showAdminInquiry (
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "closed", required = false, defaultValue = "all") String closed,
            @RequestParam(value = "searchType", required = false, defaultValue = "strategy") String searchType,
            @RequestParam(value = "searchText", required = false) String searchText) {

        if (page < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "페이지가 0보다 작습니다"));
        }

        try {
            InquiryClosed inquiryClosed = InquiryClosed.ofParameter(closed);
            InquirySearchType inquirySearchType = InquirySearchType.ofParameter(searchType);

            InquiryAdminListShowRequestDto inquiryAdminListShowRequestDto = InquiryAdminListShowRequestDto.builder()
                    .closed(inquiryClosed)
                    .searchType(inquirySearchType)
                    .searchText(searchText)
                    .build();

            PageResponse<InquiryAdminListOneShowResponseDto> adminInquiryPage = inquiryService.findInquiriesAdmin(inquiryAdminListShowRequestDto, page);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(adminInquiryPage));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
        }
    }


    /*
        관리자 문의 상세 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의의 상세 데이터 조회에 성공했을 때 : OK
        3. 해당 문의를 찾지 못했을 때 : NOT_FOUND
        4. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/qna/{qnaId}")
    public ResponseEntity<APIResponse<InquiryDetailAdminShowResponseDto>> showAdminInquiryDetail (
            @PathVariable(value = "qnaId") Long inquiryId,
            @RequestParam(value = "closed", required = false, defaultValue = "all") String closed,
            @RequestParam(value = "searchType", required = false, defaultValue = "strategy") String searchType,
            @RequestParam(value = "searchText", required = false) String searchText) {

        try {

            InquiryClosed inquiryClosed = InquiryClosed.ofParameter(closed);
            InquirySearchType inquirySearchType = InquirySearchType.ofParameter(searchType);

            InquiryDetailAdminShowDto inquiryDetailAdminShowDto = InquiryDetailAdminShowDto.builder()
                    .inquiryId(inquiryId)
                    .closed(inquiryClosed)
                    .searchType(inquirySearchType)
                    .searchText(searchText)
                    .build();

            InquiryDetailAdminShowResponseDto inquiryDetailAdminShowResponseDto = inquiryService.getInquiryAdminDetail(inquiryDetailAdminShowDto);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(inquiryDetailAdminShowResponseDto));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
        }
    }


    /*
        관리자 문의 삭제 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의 삭제에 성공했을 때 : OK
        3. 문의 삭제에 실패했을 때 : INTERNAL_SERVER_ERROR
        4. 해당 문의를 찾지 못했을 때 : NOT_FOUND
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin/qna/{qnaId}")
    public ResponseEntity<APIResponse<Long>> deleteAdminInquiry (
            @PathVariable(value = "qnaId") Long inquiryId) {

        try {
            if (inquiryService.deleteAdminInquiry(inquiryId)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }


    /*
        관리자 문의 목록 삭제 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의 목록 삭제에 성공했을 때 : OK
        3. 해당 문의를 찾지 못했을 때 : NOT_FOUND
        4. 문의 중 일부만 삭제에 실패했을 때 : MULTI_STATUS
        5. 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin/qna")
    public ResponseEntity<APIResponse<Map<Long, String>>> deleteAdminInquiryList(
            @RequestBody @Valid InquiryAdminListDeleteRequestDto inquiryAdminListDeleteRequestDto) {

        try {

            Map<Long, String> deleteResult = inquiryService.deleteAdminInquiryList(inquiryAdminListDeleteRequestDto);

            if (deleteResult.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                    .body(APIResponse.success(SuccessCode.OK, deleteResult));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
        }
    }


    /*
        질문자 문의 등록 화면 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 질문자 문의 등록 화면 조회에 성공했을 때 : OK
        3. 질문자 문의 등록 화면 조회에 실패했을 때 : NOT_FOUND
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/strategy/{strategyId}/qna")
    public ResponseEntity<APIResponse<InquirySavePageShowResponseDto>> showInquirySavePage (
            @PathVariable(value = "strategyId") Long strategyId) {

        try {

            InquirySavePageShowResponseDto inquirySavePageShowResponseDto =  inquiryService.getStrategyInquiryPage(strategyId);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(inquirySavePageShowResponseDto));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }


    /*
        질문자 문의 등록 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의가 등록에 성공했을 때 : OK
        3. 문의가 등록에 실패했을 때 : INTERNAL_SERVER_ERROR
        4. 데이터의 형식이 올바르지 않음 : BAD_REQUEST
        5. 등록하는 질문자나 해당 전략의 정보를 찾지 못했을 때 : NOT_FOUND
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/strategy/{strategyId}/qna")
    public ResponseEntity<APIResponse<Long>> saveInquirerInquiry(
            @PathVariable(value = "strategyId") Long strategyId,
            @RequestBody @Valid InquirySaveRequestDto inquirySaveRequestDto) {

        try {
            if (inquiryService.registerInquiry(
                    strategyId,
                    inquirySaveRequestDto)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }


    /*
        질문자 문의 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의 데이터 조회에 성공했을 때 : OK
        3. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/member/qna")
    public ResponseEntity<APIResponse<PageResponse<InquiryListOneShowResponseDto>>> showInquirerInquiry (
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "sort", defaultValue = "registrationDate") String sort,
            @RequestParam(value = "closed", defaultValue = "all") String closed) {

        if (page < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "페이지가 0보다 작습니다"));
        }

        try {

            InquiryClosed inquiryClosed = InquiryClosed.ofParameter(closed);
            InquirySort inquirySort = InquirySort.ofParameter(sort);

            PageResponse<InquiryListOneShowResponseDto> inquiryPage = inquiryService.showInquirerInquiry(page, inquirySort, inquiryClosed);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(inquiryPage));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
        }
    }


     /*
        질문자 문의 상세 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의의 상세 데이터 조회에 성공했을 때 : OK
        3. 해당 문의를 찾지 못했을 때 : NOT_FOUND
        4. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
     @Override
//    @PreAuthorize("hasRole('ROLE_USER')")
     @GetMapping("/member/qna/{qnaId}")
    public ResponseEntity<APIResponse<InquiryDetailInquirerShowResponseDto>> showInquirerInquiryDetail (
            @PathVariable(value = "qnaId") Long inquiryId,
            @RequestParam(value = "sort", defaultValue = "registrationDate") String sort,
            @RequestParam(value = "closed", defaultValue = "all") String closed) {

         try {

             InquiryClosed inquiryClosed = InquiryClosed.ofParameter(closed);
             InquirySort inquirySort = InquirySort.ofParameter(sort);

             InquiryDetailInquirerShowDto inquiryDetailInquirerShowDto = InquiryDetailInquirerShowDto.builder()
                     .inquiryId(inquiryId)
                     .closed(inquiryClosed)
                     .sort(inquirySort)
                     .build();

             InquiryDetailInquirerShowResponseDto inquiryDetailInquirerShowResponseDto = inquiryService.getInquirerInquiryDetail(inquiryDetailInquirerShowDto);

             return ResponseEntity.status(HttpStatus.OK)
                     .body(APIResponse.success(inquiryDetailInquirerShowResponseDto));
         }
         catch (EntityNotFoundException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND)
                     .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
         }
         catch (IllegalArgumentException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
         }
    }


    /*
        질문자 문의 수정 화면 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 질문자 문의 수정 화면 조회에 성공했을 때 : OK
        3. 해당 문의를 찾지 못했을 때 : NOT_FOUND
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/member/qna/{qnaId}/modify")
    public ResponseEntity<APIResponse<InquiryModifyPageShowResponseDto>> showInquiryModifyPage (
            @PathVariable(value = "qnaId") Long inquiryId) {

        try {

            InquiryModifyPageShowResponseDto inquiryModifyPageShowResponseDto = inquiryService.showInquiryModifyPage(inquiryId);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(inquiryModifyPageShowResponseDto));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }


     /*
        질문자 문의 수정 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의 수정에 성공했을 때 : OK
        3. 문의 수정에 실패했을 때 : INTERNAL_SERVER_ERROR
        4. 해당 문의를 찾지 못했을 때 : NOT_FOUND
        5. 데이터의 형식이 올바르지 않음 : BAD_REQUEST
            +) 답변이 등록된 문의를 수정 시도함
     */
     @Override
//    @PreAuthorize("hasRole('ROLE_USER')")
     @PutMapping("/member/qna/{qnaId}")
    public ResponseEntity<APIResponse<Long>> modifyInquirerInquiry (
            @PathVariable(value = "qnaId") Long inquiryId,
            @RequestBody @Valid InquiryModifyRequestDto inquiryModifyRequestDto) {

        try {

            if (inquiryService.modifyInquiry(
                    inquiryId,
                    inquiryModifyRequestDto)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        }
        catch (IllegalStateException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }


    /*
        질문자 문의 삭제 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의 삭제에 성공했을 때 : OK
        3. 문의 삭제에 실패했을 때 : INTERNAL_SERVER_ERROR
        4. 해당 문의를 찾지 못했을 때 : NOT_FOUND
        5. 답변이 등록된 문의를 수정 시도함 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/member/qna/{qnaId}")
    public ResponseEntity<APIResponse<Long>> deleteInquirerInquiry (
            @PathVariable(value = "qnaId") Long inquiryId) {

        try {

            if (inquiryService.deleteInquiry(inquiryId)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
        }
    }


    /*
        트레이더 문의 답변 등록 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의 답변이 등록에 성공했을 때 : OK
        3. 문의 답변이 등록에 실패했을 때 : INTERNAL_SERVER_ERROR
        4. 데이터의 형식이 올바르지 않음 : BAD_REQUEST
        5. 등록하는 문의 정보를 찾지 못했을 때 : NOT_FOUND
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_TRADER')")
    @PostMapping("/trader/qna/{qnaId}")
    public ResponseEntity<APIResponse<Long>> saveTraderInquiryAnswer (
            @PathVariable(value = "qnaId") Long inquiryId,
            @RequestBody @Valid InquiryAnswerSaveRequestDto inquiryAnswerSaveRequestDto) {

        try {

            if (inquiryAnswerService.registerInquiryAnswer(
                    inquiryId,
                    inquiryAnswerSaveRequestDto)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
        }
    }


    /*
        트레이더 문의 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의 데이터 조회에 성공했을 때 : OK
        3. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_TRADER')")
    @GetMapping("/trader/qna")
    public ResponseEntity<APIResponse<PageResponse<InquiryListOneShowResponseDto>>> showTraderInquiry (
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "sort", defaultValue = "registrationDate") String sort,
            @RequestParam(value = "closed", defaultValue = "all") String closed) {

        if (page < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "페이지가 0보다 작습니다"));
        }



        try {

            InquiryClosed inquiryClosed = InquiryClosed.ofParameter(closed);
            InquirySort inquirySort = InquirySort.ofParameter(sort);

            PageResponse<InquiryListOneShowResponseDto> inquiryPage = inquiryService.showTraderInquiry(page, inquirySort, inquiryClosed);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(inquiryPage));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
        }
    }


    /*
        트레이더 문의 상세 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의의 상세 데이터 조회에 성공했을 때 : OK
        3. 해당 문의를 찾지 못했을 때 : NOT_FOUND
        4. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_TRADER')")
    @GetMapping("/trader/qna/{qnaId}")
    public ResponseEntity<APIResponse<InquiryDetailTraderShowResponseDto>> showTraderInquiryDetail (
            @PathVariable(value = "qnaId") Long inquiryId,
            @RequestParam(value = "sort", defaultValue = "registrationDate") String sort,
            @RequestParam(value = "closed", defaultValue = "all") String closed) {

        try {

            InquiryClosed inquiryClosed = InquiryClosed.ofParameter(closed);
            InquirySort inquirySort = InquirySort.ofParameter(sort);

            InquiryDetailTraderShowDto inquiryDetailTraderShowDto = InquiryDetailTraderShowDto.builder()
                    .inquiryId(inquiryId)
                    .sort(inquirySort)
                    .closed(inquiryClosed)
                    .build();

            InquiryDetailTraderShowResponseDto inquiryDetailTraderShowResponseDto = inquiryService.getTraderInquiryDetail(inquiryDetailTraderShowDto);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(inquiryDetailTraderShowResponseDto));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
        }
    }
}