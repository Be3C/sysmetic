package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.InquiryAnswerSaveRequestDto;
import com.be3c.sysmetic.domain.member.entity.Inquiry;
import com.be3c.sysmetic.domain.member.entity.InquiryAnswer;
import com.be3c.sysmetic.domain.member.entity.InquiryStatus;
import com.be3c.sysmetic.domain.member.entity.Member;
import com.be3c.sysmetic.domain.member.exception.MemberExceptionMessage;
import com.be3c.sysmetic.domain.member.message.InquiryExceptionMessage;
import com.be3c.sysmetic.domain.member.repository.InquiryAnswerRepository;
import com.be3c.sysmetic.domain.member.repository.InquiryRepository;
import com.be3c.sysmetic.domain.member.repository.MemberRepository;
import com.be3c.sysmetic.global.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InquiryAnswerServiceImpl implements InquiryAnswerService {

    private final SecurityUtils securityUtils;

    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    //등록
    @Override
    @Transactional
    public boolean registerInquiryAnswer(Long inquiryId, InquiryAnswerSaveRequestDto inquiryAnswerSaveRequestDto) {

        Long traderId = securityUtils.getUserIdInSecurityContext();

        Inquiry inquiry = inquiryRepository.findByIdAndTraderAndStatusCode(inquiryId, traderId).orElseThrow(() -> new EntityNotFoundException(InquiryExceptionMessage.NOT_FOUND_INQUIRY.getMessage()));

        if(inquiry.getInquiryStatus() == InquiryStatus.closed) {
            throw new IllegalStateException(InquiryExceptionMessage.INQUIRY_CLOSED.getMessage());
        }

        Member trader = memberRepository.findById(traderId).orElseThrow(() -> new EntityNotFoundException(MemberExceptionMessage.DATA_NOT_FOUND.getMessage()));

        InquiryAnswer inquiryAnswer = InquiryAnswer.createInquiryAnswer(inquiry, trader, inquiryAnswerSaveRequestDto.getAnswerTitle(), inquiryAnswerSaveRequestDto.getAnswerContent());
        inquiryAnswerRepository.save(inquiryAnswer);

        inquiry.setInquiryStatus(InquiryStatus.closed);
        inquiryRepository.save(inquiry);

        return true;
    }
}
