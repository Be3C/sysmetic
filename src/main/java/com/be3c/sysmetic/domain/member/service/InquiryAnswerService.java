package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.InquiryDetailSaveRequestDto;
import com.be3c.sysmetic.domain.member.entity.InquiryAnswer;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InquiryAnswerService {

    //등록
    @Transactional
    boolean registerInquiryAnswer(Long inquiryId, InquiryDetailSaveRequestDto inquiryDetailSaveRequestDto);
}
