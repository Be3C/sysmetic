package com.be3c.sysmetic.domain.member.repository;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InquiryRepositoryCustom {

    Page<Inquiry> adminInquirySearchWithBooleanBuilder(InquiryAdminListShowRequestDto inquiryAdminListShowRequestDto, Pageable pageable);

    Page<Inquiry> registrationDateTraderInquirySearchWithBooleanBuilder(InquiryTraderListShowRequestDto inquiryTraderListShowRequestDto, Pageable pageable);

    Page<Inquiry> registrationDateInquirerInquirySearchWithBooleanBuilder(InquiryInquirerListShowRequestDto inquiryInquirerListShowRequestDto, Pageable pageable);

    Page<Inquiry> strategyNameTraderInquirySearchWithBooleanBuilder(InquiryTraderListShowRequestDto inquiryTraderListShowRequestDto, Pageable pageable);

    List<Inquiry> strategyNameInquirerInquirySearchWithBooleanBuilder(InquiryInquirerListShowRequestDto inquiryInquirerListShowRequestDto);

    Inquiry adminFindPreviousInquiry(InquiryDetailAdminShowDto inquiryDetailAdminShowDto);
    Inquiry adminFindNextInquiry(InquiryDetailAdminShowDto inquiryDetailAdminShowDto);

    Inquiry inquirerFindPreviousInquiryRegistrationDate(InquiryDetailInquirerShowDto inquiryDetailInquirerShowDto);
    Inquiry inquirerFindPreviousInquiryStrategyName(InquiryDetailInquirerShowDto inquiryDetailInquirerShowDto);

    Inquiry inquirerFindNextInquiryRegistrationDate(InquiryDetailInquirerShowDto inquiryDetailInquirerShowDto);
    Inquiry inquirerFindNextInquiryStrategyName(InquiryDetailInquirerShowDto inquiryDetailInquirerShowDto);

    Inquiry traderFindPreviousInquiryRegistrationDate(InquiryDetailTraderShowDto inquiryDetailTraderShowDto);
    Inquiry traderFindPreviousInquiryStrategyName(InquiryDetailTraderShowDto inquiryDetailTraderShowDto);

    Inquiry traderFindNextInquiryRegistrationDate(InquiryDetailTraderShowDto inquiryDetailTraderShowDto);
    Inquiry traderFindNextInquiryStrategyName(InquiryDetailTraderShowDto inquiryDetailTraderShowDto);
}
