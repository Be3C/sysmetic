package com.be3c.sysmetic.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공지사항 등록 요청 DTO")
public class NoticeSaveRequestDto {

    @Schema(description = "공지사항 제목", example = "공지사항 제목 예시")
    @Size(max = 100)
    @NotBlank
    private String noticeTitle;

    @Schema(description = "공지사항 내용", example = "공지사항 내용 예시입니다.")
    @NotBlank
    private String noticeContent;

    @Schema(description = "공개 여부", example = "true")
    @NotNull
    private Boolean isOpen;
}
