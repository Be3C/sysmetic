package com.be3c.sysmetic.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.be3c.sysmetic.domain.member.entity.InquiryStatus;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문의 목록 조회 요청 DTO")
public class InquiryListShowRequestDto {

    @Schema(description = "문의자 ID", example = "1001")
    private Long inquirerId;

    @Schema(description = "트레이더 ID", example = "9876")
    private Long traderId;

    // 답변상태 셀렉트 박스
    @Schema(description = "답변 상태 (all, closed, unclosed)", example = "ALL")
    private InquiryStatus closed;
}
