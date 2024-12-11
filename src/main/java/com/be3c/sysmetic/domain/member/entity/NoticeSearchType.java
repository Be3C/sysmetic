package com.be3c.sysmetic.domain.member.entity;

import lombok.Getter;

@Getter
public enum NoticeSearchType {
    TITLE("title"),
    CONTENT("content"),
    TITLE_CONTENT("titlecontent"),
    WRITER("writer");

    private final String parameter;

    NoticeSearchType(String parameter) {
        this.parameter = parameter;
    }

    public static NoticeSearchType ofParameter(String parameter) {
        return switch (parameter) {
            case "title" -> TITLE;
            case "content" -> CONTENT;
            case "titlecontent" -> TITLE_CONTENT;
            case "writer" -> WRITER;
            default -> throw new IllegalArgumentException("쿼리 파라미터 searchType이 올바르지 않습니다 : " + parameter);
        };
    }
}
