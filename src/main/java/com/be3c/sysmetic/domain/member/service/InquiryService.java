package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.InquiryClosed;
import com.be3c.sysmetic.domain.member.entity.InquirySort;
import com.be3c.sysmetic.global.common.response.PageResponse;

import java.util.Map;

public interface InquiryService {

    // 전략 문의 등록 화면 조회
    InquirySavePageShowResponseDto getStrategyInquiryPage(Long strategyId);

    // 등록
    boolean registerInquiry(Long strategyId, InquirySaveRequestDto inquirySaveRequestDto);

    // 수정
    boolean modifyInquiry(Long inquiryId, InquiryModifyRequestDto inquiryModifyRequestDto);

    // 질문자 삭제
    boolean deleteInquiry(Long inquiryId);

    // 관리자 삭제
    boolean deleteAdminInquiry(Long inquiryId);

    // 관리자 목록 삭제
    Map<Long, String> deleteAdminInquiryList(InquiryAdminListDeleteRequestDto inquiryAdminListDeleteRequestDto);

    // 관리자 검색 조회
    // 전체, 답변 대기, 답변 완료
    // 검색 (전략명, 트레이더, 질문자)
    PageResponse<InquiryAdminListOneShowResponseDto> findInquiriesAdmin(InquiryAdminListShowRequestDto inquiryAdminListShowRequestDto, Integer page);

    InquiryDetailAdminShowResponseDto getInquiryAdminDetail(InquiryDetailAdminShowDto inquiryDetailAdminShowDto);

    InquiryDetailInquirerShowResponseDto getInquirerInquiryDetail(InquiryDetailInquirerShowDto inquiryDetailInquirerShowDto);

    InquiryDetailTraderShowResponseDto getTraderInquiryDetail(InquiryDetailTraderShowDto inquiryDetailTraderShowDto);

    // 문의자 검색 조회
    // 정렬 순 셀렉트 박스 (최신순, 전략명)
    // 답변상태 셀렉트 박스 (전체, 답변 대기, 답변 완료)
    PageResponse<InquiryListOneShowResponseDto> showInquirerInquiry(Integer page, InquirySort sort, InquiryClosed closed);

    // 트레이더 검색 조회
    // 정렬 순 셀렉트 박스 (최신순, 전략명)
    // 답변상태 셀렉트 박스 (전체, 답변 대기, 답변 완료)
    PageResponse<InquiryListOneShowResponseDto> showTraderInquiry(Integer page, InquirySort sort, InquiryClosed closed);

    InquiryModifyPageShowResponseDto showInquiryModifyPage(Long inquiryId);
}
