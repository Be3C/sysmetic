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
@Schema(description = "문의 등록 요청 DTO")
public class InquirySaveRequestDto {

    @Schema(description = "문의 제목", example = "전략에 대한 문의")
    @Size(max = 100)
    @NotBlank
    private String inquiryTitle;

    @Schema(description = "문의 내용", example = "이 전략에 대한 설명을 더 듣고 싶습니다.")
    @Size(max = 1000)
    @NotBlank
    private String inquiryContent;
}
