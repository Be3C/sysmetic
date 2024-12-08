package com.be3c.sysmetic.domain.member.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoticeFailMessage {

    NOT_FOUND_NOTICE("해당 공지를 찾을 수 없습니다."),
    NOT_FOUND_FILE("삭제하려는 파일이 이 공지사항에 존재하지 않습니다."),
    NOT_FOUND_IMAGE("삭제하려는 이미지가 이 공지사항에 존재하지 않습니다."),
    FILE_NUMBER_EXCEEDED("파일이 3개 이상입니다."),
    IMAGE_NUMBER_EXCEEDED("이미지가 5개 이상입니다.");

    private final String message;
}
