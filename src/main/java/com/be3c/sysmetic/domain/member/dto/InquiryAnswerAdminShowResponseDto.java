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
@Schema(description = "관리자 문의 답변 조회 응답 DTO")
public class InquiryAnswerAdminShowResponseDto {

    @Schema(description = "지정했던 답변상태", example = "searchType")
    private String closed;

    @Schema(description = "검색했던 검색 조건", example = "searchType")
    private String searchType;

    @Schema(description = "검색했던 검색 단어", example = "searchText")
    private String searchText;

    @Schema(description = "문의 ID", example = "12345")
    private Long inquiryId;

    @Schema(description = "문의 답변 ID", example = "54321")
    private Long inquiryAnswerId;

    @Schema(description = "문의 제목", example = "Strategy Inquiry")
    private String inquiryTitle;

    @Schema(description = "문의 등록 일시", example = "2024-11-22T15:30:00")
    private LocalDateTime inquiryRegistrationDate;

    @Schema(description = "질문자 닉네임", example = "InquirerNick")
    private String inquirerNickname;

    @Schema(description = "문의 상태", example = "closed")
    private InquiryStatus inquiryStatus;

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

    @Schema(description = "트레이더 ID", example = "12345")
    private Long traderId;

    @Schema(description = "트레이더 닉네임", example = "TraderNick")
    private String traderNickname;

    @Schema(description = "트레이더 프로필 이미지 경로", example = "/path")
    private String traderProfileImagePath;

    @Schema(description = "문의 내용", example = "What is the strategy?")
    private String inquiryContent;

    @Schema(description = "답변 제목", example = "Strategy Details")
    private String answerTitle;

    @Schema(description = "답변 등록 일시", example = "2024-11-22T16:00:00")
    private LocalDateTime answerRegistrationDate;

    @Schema(description = "답변 내용", example = "Here are the details of the strategy.")
    private String answerContent;

    @Schema(description = "이전 문의 ID", example = "123")
    private Long previousId;

    @Schema(description = "이전 문의 제목", example = "문의 제목 예")
    private String previousTitle;

    @Schema(description = "이전 문의 작성일시", example = "2023-11-21T10:15:30")
    private LocalDateTime previousWriteDate;

    @Schema(description = "다음 문의 ID", example = "123")
    private Long nextId;

    @Schema(description = "다음 문의 제목", example = "문의 제목 예")
    private String nextTitle;

    @Schema(description = "다음 문의 작성일시", example = "2023-11-21T10:15:30")
    private LocalDateTime nextWriteDate;
}
