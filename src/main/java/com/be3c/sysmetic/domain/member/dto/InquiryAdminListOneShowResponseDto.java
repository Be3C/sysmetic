package com.be3c.sysmetic.domain.member.dto;

import com.be3c.sysmetic.domain.member.entity.InquiryStatus;
import com.be3c.sysmetic.domain.strategy.dto.StockListDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 문의 상세 조회 응답 DTO")
public class InquiryAdminListOneShowResponseDto {

    @Schema(description = "문의 ID", example = "12345")
    private Long inquiryId;

    @Schema(description = "트레이더 ID", example = "12345")
    private Long traderId;

    @Schema(description = "트레이더 닉네임", example = "TraderNick")
    private String traderNickname;

    @Schema(description = "매매방식 ID", example = "12345")
    private Long methodId;

    @Schema(description = "매매방식 아이콘", example = "/path")
    private String methodIconPath;

    @Schema(description = "주기", example = "Strategy A")
    private Character cycle;

    @Schema(description = "종목 리스트 응답용 Dto", example = "private HashSet<Long> stockIds;\n" +
            "    private HashSet<String> stockNames;\n" +
            "    private HashSet<String> stockIconPath;")
    private StockListDto stockList;

    @Schema(description = "전략 ID", example = "12345")
    private Long strategyId;

    @Schema(description = "전략 이름", example = "Strategy A")
    private String strategyName;

    @Schema(description = "전략 상태 코드", example = "PUBLIC")
    private String statusCode;

    @Schema(description = "문의 등록 일시", example = "2024-11-22T15:30:00")
    private LocalDateTime inquiryRegistrationDate;

    @Schema(description = "질문자 닉네임", example = "InquirerNick")
    private String inquirerNickname;

    @Schema(description = "문의 상태", example = "closed")
    private InquiryStatus inquiryStatus;
}