package com.be3c.sysmetic.domain.member.dto;

import com.be3c.sysmetic.domain.member.entity.InquiryClosed;
import com.be3c.sysmetic.domain.member.entity.InquirySearchType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 상세 조회 쿼리 파라미터 DTO")
public class InquiryDetailAdminShowDto {

    @Schema(description = "지금 문의 ID", example = "123")
    private Long inquiryId;

    @Schema(description = "답변 상태 탭 (all, closed, unclosed) (전체, 답변완료, 답변대기)", example = "ALL")
    private InquiryClosed closed; // ALL, CLOSED, UNCLOSED

    @Schema(description = "검색 유형 (strategy, trader, inquirer) (전략명, 트레이더, 질문자)", example = "strategy")
    private InquirySearchType searchType; // 전략명, 트레이더, 질문자

    @Schema(description = "검색 텍스트", example = "트레이더1")
    private String searchText;
}