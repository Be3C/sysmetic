package com.be3c.sysmetic.domain.strategy.controller;

import com.be3c.sysmetic.global.common.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "엑셀 API", description = "엑셀 업로드와 다운로드를 수행합니다")
public interface ExcelController {

    @Operation(
            summary = "엑셀 파일 양식 URL 조회",
            description = "엑셀 파일 양식 URL을 반환합니다. 이 URL을 사용하여 엑셀 파일을 다운로드할 수 있습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "엑셀 파일 양식 URL 반환 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @GetMapping("/daily")
    ResponseEntity<APIResponse<String>> getExcelForm();

    @Operation(
            summary = "엑셀 파일 업로드",
            description = "전략 ID에 해당하는 일간 데이터를 엑셀 파일로 업로드하여 저장합니다. " +
                    "Excel 2007 이상의 .xlsx만 가능하며, 한 개의 시트만 읽습니다. " +
                    "날짜, 입출금, 일손익 세 개 컬럼의 형식이 지켜져야 합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "엑셀 파일 업로드 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (파일 형식 불일치 등)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "전략을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("permitAll()")
    @PostMapping("/daily/{strategyId}")
    ResponseEntity<APIResponse<String>> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long strategyId);

    @Operation(
            summary = "일별 거래 내역 엑셀 다운로드",
            description = "전략 ID에 해당하는 일별 거래 내역 엑셀 파일을 다운로드합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "엑셀 파일 다운로드 성공",
                            content = @Content(
                                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    schema = @Schema(implementation = InputStreamResource.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "전략을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @GetMapping("/daily/{strategyId}")
    ResponseEntity<InputStreamResource> downloadDailyExcel(
            @PathVariable Long strategyId);

    @Operation(
            summary = "일별 거래 내역과 통계 데이터 엑셀 다운로드",
            description = "전략 ID에 해당하는 일별 통계 포함 엑셀 파일을 다운로드합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "엑셀 파일 다운로드 성공",
                            content = @Content(
                                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    schema = @Schema(implementation = InputStreamResource.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "전략을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @GetMapping("/daily/statistics/{strategyId}")
    ResponseEntity<InputStreamResource> downloadDailyExcelWithStatistics(
            @PathVariable Long strategyId);

    @Operation(
            summary = "월별 엑셀 다운로드",
            description = "전략 ID에 해당하는 월별 엑셀 파일을 다운로드합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "엑셀 파일 다운로드 성공",
                            content = @Content(
                                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    schema = @Schema(implementation = InputStreamResource.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "전략을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = APIResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @GetMapping("/monthly/{strategyId}")
    ResponseEntity<InputStreamResource> downloadMonthlyExcel(
            @PathVariable Long strategyId);

}
