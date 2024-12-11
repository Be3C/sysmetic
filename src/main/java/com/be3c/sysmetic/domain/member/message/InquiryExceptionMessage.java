package com.be3c.sysmetic.domain.member.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryExceptionMessage {

    NOT_FOUND_INQUIRY("해당 문의를 찾을 수 없습니다."),
    NOT_FOUND_INQUIRY_ANSWER("해당 문의 답변을 찾을 수 없습니다."),
    INQUIRY_CLOSED("해당 문의는 이미 답변 완료되었습니다.");

    private final String message;
}
