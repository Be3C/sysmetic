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

public class InquiryAnswerDataDto {

    @Schema(description = "답변 ID", example = "123")
    private Long inquiryAnswerId;

    @Schema(description = "답변 제목", example = "Strategy Details")
    private String answerTitle;

    @Schema(description = "답변 등록 일시", example = "2024-11-22T16:00:00")
    private LocalDateTime answerRegistrationDate;

    @Schema(description = "답변 내용", example = "Here are the details of the strategy.")
    private String answerContent;
}
