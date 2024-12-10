package com.be3c.sysmetic.domain.member.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoticeExceptionMessage {

    NOT_FOUND_NOTICE("해당 공지를 찾을 수 없습니다."),
    NOT_FOUND_FILE("삭제하려는 파일이 이 공지사항에 존재하지 않습니다."),
    FILE_NUMBER_EXCEEDED("파일이 허용 개수를 초과했습니다.");

    private final String message;
}
