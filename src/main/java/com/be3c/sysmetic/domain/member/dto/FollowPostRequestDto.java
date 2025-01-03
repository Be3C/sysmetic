package com.be3c.sysmetic.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "팔로우 추가 요청 DTO")
public class FollowPostRequestDto {

    @NotNull
    @Schema(description = "폴더 ID", example = "1")
    @JsonProperty("folderId")
    private Long folderId;

    @NotNull
    @Schema(description = "전략 ID", example = "10")
    @JsonProperty("strategyId")
    private Long strategyId;
}
