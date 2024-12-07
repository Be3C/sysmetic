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
@Schema(description = "트레이더, 질문자 상세 조회 쿼리 파라미터 DTO")
public class InquiryDetailTraderInquirerShowDto {

    @Schema(description = "지금 문의 ID", example = "123")
    private Long inquiryId;

    @Schema(description = "문의자 ID", example = "1001")
    private Long inquirerId;

    @Schema(description = "트레이더 ID", example = "9876")
    private Long traderId;

    @Schema(description = "답변 상태 탭 (all, closed, unclosed) (전체, 답변완료, 답변대기)", example = "ALL")
    private InquiryStatus closed; // ALL, CLOSED, UNCLOSED

    @Schema(description = "정렬 순서 (registrationDate, strategyName) ('최신순', '전략명')", example = "최신순")
    private String sort;
}
