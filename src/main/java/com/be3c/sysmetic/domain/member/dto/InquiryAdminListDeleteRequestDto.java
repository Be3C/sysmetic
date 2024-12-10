package com.be3c.sysmetic.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문의 목록 삭제 요청 DTO")
public class InquiryAdminListDeleteRequestDto {

    @Schema(description = "삭제할 문의 ID 리스트", example = "[1, 2, 3]")
    private List<Long> inquiryIdList;
}
