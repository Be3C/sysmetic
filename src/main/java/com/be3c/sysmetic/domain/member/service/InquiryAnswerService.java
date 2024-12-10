package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.InquiryAnswerSaveRequestDto;
import org.springframework.transaction.annotation.Transactional;

public interface InquiryAnswerService {

    //등록
    @Transactional
    boolean registerInquiryAnswer(Long inquiryId, InquiryAnswerSaveRequestDto inquiryAnswerSaveRequestDto);
}
