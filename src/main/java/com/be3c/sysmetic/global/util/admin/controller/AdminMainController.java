package com.be3c.sysmetic.global.util.admin.controller;

import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.util.admin.dto.AdminMainResponseDto;
import com.be3c.sysmetic.global.util.admin.dto.RunReportResponseDto;
import com.be3c.sysmetic.global.util.admin.service.AdminMainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "관리자 메인 페이지 API", description = "관리자 메인 페이지 API")
public class AdminMainController {

    private final AdminMainService adminMainService;

    @Operation(
            summary = "메인 페이지 API",
            description = "메인 페이지 데이터 요청 API"
    )
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @GetMapping("/v1/admin/main")
    public ResponseEntity<APIResponse<AdminMainResponseDto>> getAdminMain() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success(adminMainService.getAdminMain()));
    }

    @Operation(
            summary = "구글 통계 조회",
            description = "기간 별 구글 통계 조회"
    )
    @PreAuthorize("hasRole('ROLE_MANAGER') OR hasRole('ROLE_ADMIN')")
    @GetMapping("/v1/admin/main/analytics/{period}")
    public ResponseEntity<APIResponse<RunReportResponseDto>> getAdminMainAnalytics(
            @NotBlank @PathVariable String period
    ) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(adminMainService.getAnalytics(period)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        }
    }
}
