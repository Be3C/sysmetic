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
@Schema(description = "이전 다음 문의 정보 DTO")

public class InquiryPreviousNextDto {

    @Schema(description = "이전 다음 문의 ID", example = "123")
    private Long inquiryId;

    @Schema(description = "이전 다음 문의 제목", example = "새로운 문의 제목")
    private String inquiryTitle;

    @Schema(description = "이전 다음 문의 작성일시", example = "2023-11-21T10:15:30")
    private LocalDateTime inquiryWriteDate;
}
