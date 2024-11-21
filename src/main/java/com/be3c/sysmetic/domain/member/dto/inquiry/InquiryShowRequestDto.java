package com.be3c.sysmetic.domain.member.dto.inquiry;

import io.swagger.v3.oas.annotations.media.Schema;
import com.be3c.sysmetic.domain.member.entity.InquiryStatus;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InquiryShowRequestDto {

    // 멤버 아이디
    @Schema(description = "문의자 ID", example = "1001")
    private Long inquirerId;

    // 정렬 순 셀렉트 박스
    @Schema(description = "정렬 순서 ('최신순', '전략명')", example = "최신순")
    private String sort;

    // 답변상태 셀렉트 박스
    @Schema(description = "답변 상태 (ALL, CLOSED, UNCLOSED)", example = "ALL")
    private InquiryStatus tab;
}
