package com.be3c.sysmetic.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Code {
    USING_STATE("US001"),
    NOT_USING_STATE("US002"),

    //이력 관리 코드
    FOLLOW("FS001"),
    UNFOLLOW("FS002"),

    // 비밀번호 변경 결과 코드
    PASSWORD_CHANGE_SUCCESS("PC001"),
    PASSWORD_CHANGE_FAIL("PC002"),

    // 메일 수신 동의 상태
    RECEIVE_MAIL("RM001"),
    NOT_RECEIVE_MAIL("RM002"),

    //전략 승인 관리 코드
    APPROVE_WAIT("SA001"),
    APPROVE_SUCCESS("SA002"),
    APPROVE_REJECT("SA003"),
    APPROVE_CANCEL("SA004"),

    // 전략 상태 코드
    OPEN_STRATEGY("ST001"),
    CLOSE_STRATEGY("ST002"),
    WAIT_STRATEGY("ST003"),

    // 팔로우 메일 전송 여부 코드
    SEND_FOLLOW_MAIL("SM001"),
    NOT_SEND_FOLLOW_MAIL("SM002"),

    // 회원 등급
    ROLE_USER("UR001"),
    ROLE_TRADER("UR002"),
    ROLE_MANAGER("UR003"),
    ROLE_ADMIN("UR004"),

    // 문의 답변상태 코드
    UNCLOSED_INQUIRY("IC001"),
    CLOSED_INQUIRY("IC002");

    private String code;
}
