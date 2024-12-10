package com.be3c.sysmetic.domain.member.dto;

import com.be3c.sysmetic.domain.member.entity.InquiryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "트레이더 문의 목록 조회 요청 DTO")
public class InquiryTraderListShowRequestDto {

    @Schema(description = "트레이더 ID", example = "9876")
    private Long traderId;

    // 답변상태 셀렉트 박스
    @Schema(description = "답변 상태 (all, closed, unclosed)", example = "ALL")
    private InquiryStatus closed;
}
