package com.be3c.sysmetic.domain.member.entity;

import lombok.Getter;

@Getter
public enum InquiryClosed {
    ALL("all"),
    CLOSED("closed"),
    UNCLOSED("unclosed");

    private final String parameter;

    InquiryClosed(String parameter) {
        this.parameter = parameter;
    }

    public static InquiryClosed ofParameter(String parameter) {
        return switch (parameter) {
            case "all" -> ALL;
            case "closed" -> CLOSED;
            case "unclosed" -> UNCLOSED;
            default -> throw new IllegalArgumentException("쿼리 파라미터 closed가 올바르지 않습니다 : " + parameter);
        };
    }
}
