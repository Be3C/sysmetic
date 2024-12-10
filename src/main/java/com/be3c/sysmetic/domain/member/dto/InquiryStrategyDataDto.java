package com.be3c.sysmetic.domain.member.dto;

import com.be3c.sysmetic.domain.strategy.dto.StockListDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이전 다음 공지사항 정보 DTO")

public class InquiryStrategyDataDto {

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
}
