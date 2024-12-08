package com.be3c.sysmetic.domain.member.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryFailMessage {

    NOT_FOUND_INQUIRY("해당 문의를 찾을 수 없습니다.");

    private final String message;
}
