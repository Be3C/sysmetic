package com.be3c.sysmetic.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 문의 답변 등록 요청 DTO")
public class InquiryDetailSaveRequestDto {

    @Schema(description = "답변 제목", example = "Strategy Inquiry Answer")
    @Size(max = 100)
    @NotBlank
    private String answerTitle;

    @Schema(description = "답변 내용", example = "Here is the detailed answer to your inquiry.")
    @Size(max = 1000)
    @NotBlank
    private String answerContent;
}