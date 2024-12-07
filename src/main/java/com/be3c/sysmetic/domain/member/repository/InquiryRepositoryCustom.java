package com.be3c.sysmetic.domain.member.repository;

import com.be3c.sysmetic.domain.member.dto.InquiryAdminListShowRequestDto;
import com.be3c.sysmetic.domain.member.dto.InquiryDetailAdminShowDto;
import com.be3c.sysmetic.domain.member.dto.InquiryDetailTraderInquirerShowDto;
import com.be3c.sysmetic.domain.member.dto.InquiryListShowRequestDto;
import com.be3c.sysmetic.domain.member.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InquiryRepositoryCustom {

    Page<Inquiry> adminInquirySearchWithBooleanBuilder(InquiryAdminListShowRequestDto inquiryAdminListShowRequestDto, Pageable pageable);

    Page<Inquiry> pageInquirySearchWithBooleanBuilder(InquiryListShowRequestDto inquiryListShowRequestDto, Pageable pageable);

    List<Inquiry> listTraderInquirySearchWithBooleanBuilder(InquiryListShowRequestDto inquiryListShowRequestDto);

    List<Inquiry> listInquirerInquirySearchWithBooleanBuilder(InquiryListShowRequestDto inquiryListShowRequestDto);

    Optional<Inquiry> adminFindPreviousInquiry(InquiryDetailAdminShowDto inquiryDetailAdminShowDto);

    Optional<Inquiry> adminFindNextInquiry(InquiryDetailAdminShowDto inquiryDetailAdminShowDto);

    Optional<Inquiry> inquirerFindPreviousInquiryRegistrationDate(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto);
    Optional<Inquiry> inquirerFindPreviousInquiryStrategyName(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto);

    Optional<Inquiry> inquirerFindNextInquiryRegistrationDate(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto);
    Optional<Inquiry> inquirerFindNextInquiryStrategyName(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto);

    Optional<Inquiry> traderFindPreviousInquiryRegistrationDate(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto);
    Optional<Inquiry> traderFindPreviousInquiryStrategyName(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto);

    Optional<Inquiry> traderFindNextInquiryRegistrationDate(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto);
    Optional<Inquiry> traderFindNextInquiryStrategyName(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto);
}
