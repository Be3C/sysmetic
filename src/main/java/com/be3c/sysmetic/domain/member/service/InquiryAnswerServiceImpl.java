package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.InquiryDetailSaveRequestDto;
import com.be3c.sysmetic.domain.member.entity.Inquiry;
import com.be3c.sysmetic.domain.member.entity.InquiryAnswer;
import com.be3c.sysmetic.domain.member.entity.InquiryStatus;
import com.be3c.sysmetic.domain.member.exception.InquiryNotWriterException;
import com.be3c.sysmetic.domain.member.message.InquiryFailMessage;
import com.be3c.sysmetic.domain.member.repository.InquiryAnswerRepository;
import com.be3c.sysmetic.domain.member.repository.InquiryRepository;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.util.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InquiryAnswerServiceImpl implements InquiryAnswerService {

    private final SecurityUtils securityUtils;

    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final InquiryRepository inquiryRepository;

    //등록
    @Override
    @Transactional
    public boolean registerInquiryAnswer(Long inquiryId, InquiryDetailSaveRequestDto inquiryDetailSaveRequestDto) {

        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY.getMessage()));

        if(!Objects.equals(securityUtils.getUserIdInSecurityContext(), inquiry.getInquirer().getId())) {
            throw new InquiryNotWriterException(InquiryFailMessage.NOT_STRATEGY_TRADER.getMessage());
        }

        InquiryAnswer inquiryAnswer = InquiryAnswer.createInquiryAnswer(inquiry, inquiryDetailSaveRequestDto.getAnswerTitle(), inquiryDetailSaveRequestDto.getAnswerContent());
        inquiryAnswerRepository.save(inquiryAnswer);

        inquiry.setInquiryStatus(InquiryStatus.closed);
        inquiryRepository.save(inquiry);

        return true;
    }
}
