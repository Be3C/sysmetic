package com.be3c.sysmetic.domain.member.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryFailMessage {

    NOT_FOUND_INQUIRY("해당 문의를 찾을 수 없습니다."),
    NOT_FOUND_INQUIRY_ANSWER("해당 문의 답변을 찾을 수 없습니다."),
    NOT_INQUIRY_WRITER("해당 문의의 작성자가 아닙니다."),
    NOT_STRATEGY_TRADER("해당 문의 전략의 트레이더가 아닙니다.");

    private final String message;
}
