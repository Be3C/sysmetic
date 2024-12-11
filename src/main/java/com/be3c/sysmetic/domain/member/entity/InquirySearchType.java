package com.be3c.sysmetic.domain.member.entity;

import lombok.Getter;

@Getter
public enum InquirySearchType {
    STRATEGY("strategy"),
    TRADER("trader"),
    INQUIRER("inquirer");

    private final String parameter;

    InquirySearchType(String parameter) {
        this.parameter = parameter;
    }

    public static InquirySearchType ofParameter(String parameter) {
        return switch (parameter) {
            case "strategy" -> STRATEGY;
            case "trader" -> TRADER;
            case "inquirer" -> INQUIRER;
            default -> throw new IllegalArgumentException("쿼리 파라미터 searchType이 올바르지 않습니다 : " + parameter);
        };
    }
}
