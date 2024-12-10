package com.be3c.sysmetic.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이전 다음 공지사항 정보 DTO")

public class NoticePreviousNextDto {

    @Schema(description = "이전 다음 공지사항 ID", example = "123")
    private Long noticeId;

    @Schema(description = "이전 다음 공지사항 제목", example = "새로운 공지사항 제목")
    private String noticeTitle;

    @Schema(description = "이전 다음 공지사항 작성일시", example = "2023-11-21T10:15:30")
    private LocalDateTime noticeWriteDate;
}
