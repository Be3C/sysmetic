package com.be3c.sysmetic.domain.member.entity;

import lombok.Getter;

@Getter
public enum InquirySort {
    REGISTRATION_DATE("registrationDate"),
    STRATEGY_NAME("strategyName");

    private final String parameter;

    InquirySort(String parameter) {
        this.parameter = parameter;
    }

    public static InquirySort ofParameter(String parameter) {
        return switch (parameter) {
            case "registrationDate" -> REGISTRATION_DATE;
            case "strategyName" -> STRATEGY_NAME;
            default -> throw new IllegalArgumentException("쿼리 파라미터 sort가 올바르지 않습니다 : " + parameter);
        };
    }
}
