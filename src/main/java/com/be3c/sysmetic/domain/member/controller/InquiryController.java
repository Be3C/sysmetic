package com.be3c.sysmetic.domain.member.controller;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.Inquiry;
import com.be3c.sysmetic.domain.member.entity.InquiryStatus;
import com.be3c.sysmetic.domain.member.service.InquiryAnswerService;
import com.be3c.sysmetic.domain.member.service.InquiryService;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.domain.strategy.exception.StrategyBadRequestException;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.common.response.SuccessCode;
import com.be3c.sysmetic.global.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1")
public class InquiryController implements InquiryControllerDocs {

    private final SecurityUtils securityUtils;

    private final InquiryService inquiryService;
    private final InquiryAnswerService inquiryAnswerService;

    private final Integer pageSize = 10; // 한 페이지 크기

    /*
        관리자 문의 조회 / 검색 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의 데이터 조회에 성공했을 때 : OK
        3. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/qna")
    public ResponseEntity<APIResponse<PageResponse<InquiryAdminListOneShowResponseDto>>> showAdminInquiry (
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "closed", required = false, defaultValue = "all") String closed,
            @RequestParam(value = "searchType", required = false, defaultValue = "strategy") String searchType,
            @RequestParam(value = "searchText", required = false) String searchText) {
        InquiryStatus inquiryStatus = InquiryStatus.valueOf(closed);

        if (page < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "페이지가 0보다 작습니다"));
        }

        if (!(closed.equals("all") || closed.equals("closed") || closed.equals("unclosed"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "쿼리 파라미터 closed가 올바르지 않습니다."));
        }

        if (!(searchType.equals("strategy") || searchType.equals("trader") || searchType.equals("inquirer"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "쿼리 파라미터 searchType이 올바르지 않습니다."));
        }

        InquiryAdminListShowRequestDto inquiryAdminListShowRequestDto = InquiryAdminListShowRequestDto.builder()
                .closed(inquiryStatus)
                .searchType(searchType)
                .searchText(searchText)
                .build();

        Page<Inquiry> inquiryList = inquiryService.findInquiriesAdmin(inquiryAdminListShowRequestDto, page);

        List<InquiryAdminListOneShowResponseDto> inquiryDtoList = inquiryList.stream()
                .map(inquiryService::inquiryToInquiryAdminOneResponseDto).collect(Collectors.toList());

        PageResponse<InquiryAdminListOneShowResponseDto> adminInquiryPage = PageResponse.<InquiryAdminListOneShowResponseDto>builder()
                .currentPage(page)
                .pageSize(pageSize)
                .totalElement(inquiryList.getTotalElements())
                .totalPages(inquiryList.getTotalPages())
                .content(inquiryDtoList)
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success(adminInquiryPage));
    }


    /*
        관리자 문의 상세 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의의 상세 데이터 조회에 성공했을 때 : OK
        3. 해당 문의를 찾지 못했을 때 : NOT_FOUND
        4. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/qna/{qnaId}")
    public ResponseEntity<APIResponse<InquiryAnswerAdminShowResponseDto>> showAdminInquiryDetail (
            @PathVariable(value = "qnaId") Long inquiryId,
            @RequestParam(value = "closed", required = false, defaultValue = "all") String closed,
            @RequestParam(value = "searchType", required = false, defaultValue = "strategy") String searchType,
            @RequestParam(value = "searchText", required = false) String searchText) {
        InquiryStatus inquiryStatus = InquiryStatus.valueOf(closed);

        try {

            InquiryDetailAdminShowDto inquiryDetailAdminShowDto = InquiryDetailAdminShowDto.builder()
                    .inquiryId(inquiryId)
                    .closed(inquiryStatus)
                    .searchType(searchType)
                    .searchText(searchText)
                    .build();

            InquiryAnswerAdminShowResponseDto inquiryAnswerAdminShowResponseDto = inquiryService.inquiryIdToInquiryAnswerAdminShowResponseDto(inquiryDetailAdminShowDto);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(inquiryAnswerAdminShowResponseDto));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
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
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
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
        5. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin/qna")
    public ResponseEntity<APIResponse<Map<Long, String>>> deleteAdminInquiryList(
            @RequestBody @Valid InquiryAdminListDeleteRequestDto noticeListDeleteRequestDto) {

        List<Long> inquiryIdList = noticeListDeleteRequestDto.getInquiryIdList();

        try {
            Map<Long, String> deleteResult = inquiryService.deleteAdminInquiryList(inquiryIdList);

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
    }


    /*
        질문자 문의 등록 화면 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 질문자 문의 등록 화면 조회에 성공했을 때 : OK
        3. 질문자 문의 등록 화면 조회에 실패했을 때 : NOT_FOUND
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_USER_MANAGER")
    @GetMapping("/strategy/{strategyId}/qna")
    public ResponseEntity<APIResponse<InquirySavePageShowResponseDto>> showInquirySavePage (
            @PathVariable(value = "strategyId") Long strategyId) {

        try {
            Strategy strategy = inquiryService.findStrategyForInquiryPage(strategyId);

            InquirySavePageShowResponseDto inquirySavePageShowResponseDto = inquiryService.strategyToInquirySavePageShowResponseDto(strategy);

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
//    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_USER_MANAGER")
    @PostMapping("/strategy/{strategyId}/qna")
    public ResponseEntity<APIResponse<Long>> saveInquirerInquiry(
            @PathVariable(value = "strategyId") Long strategyId,
            @RequestBody @Valid InquirySaveRequestDto inquirySaveRequestDto) {

        Long userId = securityUtils.getUserIdInSecurityContext();

        try {
            if (inquiryService.registerInquiry(
                    userId,
                    strategyId,
                    inquirySaveRequestDto.getInquiryTitle(),
                    inquirySaveRequestDto.getInquiryContent())) {
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
//    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_USER_MANAGER")
    @GetMapping("/member/qna")
    public ResponseEntity<APIResponse<PageResponse<InquiryListOneShowResponseDto>>> showInquirerInquiry (
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "sort", defaultValue = "registrationDate") String sort,
            @RequestParam(value = "closed", defaultValue = "all") String closed) {
        InquiryStatus inquiryStatus = InquiryStatus.valueOf(closed);

        if (page < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "페이지가 0보다 작습니다"));
        }

        if (!(closed.equals("all") || closed.equals("closed") || closed.equals("unclosed"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "쿼리 파라미터 closed가 올바르지 않습니다."));
        }

        if (!(sort.equals("registrationDate") || sort.equals("strategyName"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "쿼리 파라미터 sort가 올바르지 않습니다."));
        }

        try {

            PageResponse<InquiryListOneShowResponseDto> inquiryPage = inquiryService.showInquirerInquiry(page, sort, inquiryStatus);

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
//     @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_USER_MANAGER")
     @GetMapping("/member/qna/{qnaId}")
    public ResponseEntity<APIResponse<InquiryAnswerInquirerShowResponseDto>> showInquirerInquiryDetail (
            @PathVariable(value = "qnaId") Long inquiryId,
            @RequestParam(value = "sort", defaultValue = "registrationDate") String sort,
            @RequestParam(value = "closed", defaultValue = "all") String closed) {
         InquiryStatus inquiryStatus = InquiryStatus.valueOf(closed);

         try {

             InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto = InquiryDetailTraderInquirerShowDto.builder()
                     .inquiryId(inquiryId)
                     .closed(inquiryStatus)
                     .sort(sort)
                     .build();

             InquiryAnswerInquirerShowResponseDto inquiryAnswerInquirerShowResponseDto = inquiryService.inquiryIdToInquiryAnswerInquirerShowResponseDto(inquiryDetailTraderInquirerShowDto);

             return ResponseEntity.status(HttpStatus.OK)
                     .body(APIResponse.success(inquiryAnswerInquirerShowResponseDto));
         }
         catch (EntityNotFoundException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND)
                     .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
         }
    }


    /*
        질문자 문의 수정 화면 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 질문자 문의 수정 화면 조회에 성공했을 때 : OK
        3. 해당 문의를 찾지 못했을 때 : NOT_FOUND
        4. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_USER_MANAGER")
    @GetMapping("/member/qna/{qnaId}/modify")
    public ResponseEntity<APIResponse<InquiryModifyPageShowResponseDto>> showInquiryModifyPage (
            @PathVariable(value = "qnaId") Long inquiryId) {

        try {

            Inquiry inquiry = inquiryService.findOneInquiry(inquiryId);

            if(!Objects.equals(securityUtils.getUserIdInSecurityContext(), inquiry.getInquirer().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(APIResponse.fail(ErrorCode.FORBIDDEN, "유효하지 않은 회원입니다."));
            }

            InquiryModifyPageShowResponseDto inquiryModifyPageShowResponseDto = InquiryModifyPageShowResponseDto.builder()
                    .inquiryTitle(inquiry.getInquiryTitle())
                    .inquiryContent(inquiry.getInquiryContent())
                    .build();

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
//     @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_USER_MANAGER")
     @PutMapping("/member/qna/{qnaId}")
    public ResponseEntity<APIResponse<Long>> modifyInquirerInquiry (
            @PathVariable(value = "qnaId") Long inquiryId,
            @RequestBody @Valid InquiryModifyRequestDto inquiryModifyRequestDto) {

        try {

            if (inquiryService.modifyInquiry(
                    inquiryId,
                    inquiryModifyRequestDto.getInquiryTitle(),
                    inquiryModifyRequestDto.getInquiryContent())) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        }
        catch (IllegalStateException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "답변이 등록된 문의는 수정할 수 없습니다."));
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
//    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_USER_MANAGER")
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
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "답변이 등록된 문의는 수정할 수 없습니다."));
        }
        catch (StrategyBadRequestException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(APIResponse.fail(e.getErrorCode(), e.getMessage()));
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
//    @PreAuthorize("hasRole('ROLE_TRADER') or hasRole('ROLE_TRADER_MANAGER')")
    @PostMapping("/trader/qna/{qnaId}")
    public ResponseEntity<APIResponse<Long>> saveTraderInquiryAnswer (
            @PathVariable(value = "qnaId") Long inquiryId,
            @RequestBody @Valid InquiryDetailSaveRequestDto inquiryDetailSaveRequestDto) {

        try {
            Inquiry inquiry = inquiryService.findOneInquiry(inquiryId);

            if(!Objects.equals(securityUtils.getUserIdInSecurityContext(), inquiry.getTrader().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(APIResponse.fail(ErrorCode.FORBIDDEN, "유효하지 않은 회원입니다."));
            }

            if (inquiryAnswerService.registerInquiryAnswer(
                    inquiryId,
                    inquiryDetailSaveRequestDto.getAnswerTitle(),
                    inquiryDetailSaveRequestDto.getAnswerContent())) {
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
        트레이더 문의 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 문의 데이터 조회에 성공했을 때 : OK
        3. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_TRADER') or hasRole('ROLE_TRADER_MANAGER')")
    @GetMapping("/trader/qna")
    public ResponseEntity<APIResponse<PageResponse<InquiryListOneShowResponseDto>>> showTraderInquiry (
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "sort", defaultValue = "registrationDate") String sort,
            @RequestParam(value = "closed", defaultValue = "all") String closed) {
        InquiryStatus inquiryStatus = InquiryStatus.valueOf(closed);

        if (page < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "페이지가 0보다 작습니다"));
        }

        if (!(closed.equals("all") || closed.equals("closed") || closed.equals("unclosed"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "쿼리 파라미터 closed가 올바르지 않습니다."));
        }

        if (!(sort.equals("registrationDate") || sort.equals("strategyName"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "쿼리 파라미터 sort가 올바르지 않습니다."));
        }

        try {

            PageResponse<InquiryListOneShowResponseDto> inquiryPage = inquiryService.showTraderInquiry(page, sort, inquiryStatus);

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
//    @PreAuthorize("hasRole('ROLE_TRADER') or hasRole('ROLE_TRADER_MANAGER')")
    @GetMapping("/trader/qna/{qnaId}")
    public ResponseEntity<APIResponse<InquiryAnswerTraderShowResponseDto>> showTraderInquiryDetail (
            @PathVariable(value = "qnaId") Long inquiryId,
            @RequestParam(value = "sort", defaultValue = "registrationDate") String sort,
            @RequestParam(value = "closed", defaultValue = "all") String closed) {
        InquiryStatus inquiryStatus = InquiryStatus.valueOf(closed);

        try {

            InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto = InquiryDetailTraderInquirerShowDto.builder()
                    .inquiryId(inquiryId)
                    .sort(sort)
                    .closed(inquiryStatus)
                    .build();

            InquiryAnswerTraderShowResponseDto inquiryAnswerTraderShowResponseDto = inquiryService.inquiryIdToInquiryAnswerTraderShowResponseDto(inquiryDetailTraderInquirerShowDto);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(inquiryAnswerTraderShowResponseDto));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }
}