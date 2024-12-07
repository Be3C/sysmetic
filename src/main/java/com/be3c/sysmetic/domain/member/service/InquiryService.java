package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.Inquiry;
import com.be3c.sysmetic.domain.member.entity.InquiryStatus;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.global.common.response.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface InquiryService {

    // 문의 단건 조회
    Inquiry findOneInquiry(Long inquiryId);

    // 전략 문의 등록 화면 조회
    Strategy findStrategyForInquiryPage(Long strategyId);

    // 등록
    boolean registerInquiry(Long memberId, Long strategyId, String inquiryTitle, String inquiryContent);

    // 수정
    boolean modifyInquiry(Long inquiryId, String inquiryTitle, String inquiryContent);

    // 질문자 삭제
    boolean deleteInquiry(Long inquiryId);

    // 관리자 삭제
    boolean deleteAdminInquiry(Long inquiryId);

    // 관리자 목록 삭제
    Map<Long, String> deleteAdminInquiryList(List<Long> inquiryIdList);

    // 관리자 검색 조회
    // 전체, 답변 대기, 답변 완료
    // 검색 (전략명, 트레이더, 질문자)
    Page<Inquiry> findInquiriesAdmin(InquiryAdminListShowRequestDto inquiryAdminListShowRequestDto, Integer page);

    InquiryAdminListOneShowResponseDto inquiryToInquiryAdminOneResponseDto(Inquiry inquiry);

    InquiryAnswerAdminShowResponseDto inquiryIdToInquiryAnswerAdminShowResponseDto (InquiryDetailAdminShowDto inquiryDetailAdminShowDto);

    InquiryListOneShowResponseDto InquiryToInquiryInquirerOneResponseDto(Inquiry inquiry);

    InquiryListOneShowResponseDto inquiryToInquirytraderOneResponseDto(Inquiry inquiry);

    InquirySavePageShowResponseDto strategyToInquirySavePageShowResponseDto(Strategy strategy);

    InquiryAnswerInquirerShowResponseDto inquiryIdToInquiryAnswerInquirerShowResponseDto(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto);

    InquiryAnswerTraderShowResponseDto inquiryIdToInquiryAnswerTraderShowResponseDto(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto);

    // 문의자 검색 조회
    // 정렬 순 셀렉트 박스 (최신순, 전략명)
    // 답변상태 셀렉트 박스 (전체, 답변 대기, 답변 완료)
    PageResponse<InquiryListOneShowResponseDto> showInquirerInquiry(Integer page, String sort, InquiryStatus inquiryStatus);

    // 트레이더 검색 조회
    // 정렬 순 셀렉트 박스 (최신순, 전략명)
    // 답변상태 셀렉트 박스 (전체, 답변 대기, 답변 완료)
    PageResponse<InquiryListOneShowResponseDto> showTraderInquiry(Integer page, String sort, InquiryStatus inquiryStatus);
}
