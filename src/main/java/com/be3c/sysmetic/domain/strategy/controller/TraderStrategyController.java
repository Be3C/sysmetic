package com.be3c.sysmetic.domain.strategy.controller;

import com.be3c.sysmetic.domain.strategy.dto.*;
import com.be3c.sysmetic.domain.strategy.dto.AccountImageRequestDto;
import com.be3c.sysmetic.domain.strategy.dto.DailyPostResponseDto;
import com.be3c.sysmetic.domain.strategy.dto.StrategyPostRequestDto;
import com.be3c.sysmetic.domain.strategy.service.*;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Tag(name = "트레이더 전략 API", description = "트레이더 전략 관리 및 조회")
@RequestMapping("/v1/trader")
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
@RestController
public class TraderStrategyController {

    private final TraderStrategyServiceImpl traderStrategyService;
    private final DailyServiceImpl dailyService;
    private final MonthlyServiceImpl monthlyService;
    private final StrategyStatisticsServiceImpl strategyStatisticsService;
    private final AccountImageServiceImpl accountImageService;
    private final StrategyServiceImpl strategyService;

    // 전략 등록
    @Operation(
            summary = "전략 등록",
            description = "트레이더가 본인의 전략을 등록"
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @PostMapping(value = "/strategy", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<APIResponse<StrategyPostResponseDto>> insertStrategy(
            @RequestPart("requestDto") @Parameter(description = "전략 등록 요청 DTO") @Valid StrategyPostRequestDto requestDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success(traderStrategyService.insertStrategy(requestDto, file)));
    }

    // 전략 수정
    @Operation(
            summary = "전략 수정",
            description = "트레이더가 본인의 전략을 수정"
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @PatchMapping(value = "/strategy/{strategyId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<APIResponse> updateStrategy(
            @PathVariable Long strategyId,
            @RequestPart @Valid StrategyPostRequestDto requestDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        traderStrategyService.updateStrategy(strategyId, requestDto, file);
        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success());
    }

    // 전략 삭제
    @Operation(
            summary = "전략 삭제",
            description = "트레이더가 본인의 전략 목록을 삭제"
    )
     @PreAuthorize("hasRole('ROLE_TRADER')")
    @DeleteMapping("/strategy")
    public ResponseEntity<APIResponse> deleteStrategy(@RequestBody StrategyDeleteRequestDto requestDto) {
        traderStrategyService.deleteStrategy(requestDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success());
    }

    // todo. batch insert, delete
    // 일간분석 등록
    @Operation(
            summary = "일간분석 등록",
            description = "트레이더가 본인의 일간분석 데이터를 등록"
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @PostMapping("/strategy/daily/{strategyId}")
    public ResponseEntity<APIResponse> insertDaily(
            @PathVariable Long strategyId,
            @Valid @RequestBody List<DailyRequestDto> requestDtoList
    ) {
        dailyService.saveDaily(strategyId, requestDtoList);
        DailyPostResponseDto responseDto = dailyService.getIsDuplicate(strategyId, requestDtoList);

        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success(responseDto));
    }

    // 일간데이터 수정
    @Operation(
            summary = "일간분석 수정",
            description = "트레이더가 본인의 일간분석 데이터를 수정"
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @PatchMapping("/strategy/daily/{dailyId}")
    public ResponseEntity<APIResponse> updateDaily(
            @PathVariable Long dailyId,
            @RequestBody DailyRequestDto requestDto
    ) {
        dailyService.updateDaily(dailyId, requestDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success());
    }

    // 일간데이터 삭제
    @Operation(
            summary = "일간분석 삭제",
            description = "트레이더가 본인의 일간분석 데이터를 삭제"
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @DeleteMapping("/strategy/daily/{dailyId}")
    public ResponseEntity<APIResponse> deleteDaily(@PathVariable Long dailyId) {
        dailyService.deleteDaily(dailyId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success());
    }

    // 실계좌이미지 삭제
    @Operation(
            summary = "실계좌이미지 삭제",
            description = "트레이더가 본인의 실계좌이미지 삭제"
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @DeleteMapping("/strategy/account-image")
    public ResponseEntity<APIResponse> deleteAccountImage(@RequestBody AccountImageDeleteRequestDto accountImageIdList) {
        accountImageService.deleteAccountImage(accountImageIdList);
        return ResponseEntity.ok(APIResponse.success());
    }

    // 실계좌이미지 등록
    @Operation(
            summary = "실계좌이미지 등록",
            description = "트레이더가 본인의 실계좌이미지 등록"
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @PostMapping(value = "/strategy/account-image/{strategyId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<APIResponse> saveAccountImage(
            @RequestPart List<AccountImageRequestDto> requestDtoList,
            @RequestPart List<MultipartFile> images,
            @PathVariable Long strategyId
    ) {
        accountImageService.saveAccountImage(strategyId, requestDtoList, images);
        return ResponseEntity.ok(APIResponse.success());
    }

    // 전략 관리 화면 매매방식, 종목 조회
    @Operation(
            summary = "매매방식, 종목 조회",
            description = "사용중인 모든 매매방식 및 종목 조회"
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @GetMapping("/strategy/method-and-stock")
    public ResponseEntity<APIResponse<MethodAndStockGetResponseDto>> findMethodAndStockList() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success(strategyService.findMethodAndStock()));
    }

    @Operation(
            summary = "나의 전략 목록 조회",
            description = "자신이 등록한 전략 목록 조회"
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @GetMapping("/member/strategy/{page}")
    public ResponseEntity<APIResponse<MyStrategyListResponseDto>> getMyStrategyList(
            @PathVariable Integer page
    ) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(traderStrategyService.getMyStrategyList(page)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    // 비공개 전환
    @Operation(
            summary = "비공개 전환",
            description = "트레이더가 본인의 전략을 비공개로 전환"
    )
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @PatchMapping("/strategy/{id}/visibility")
    public ResponseEntity<APIResponse<String>> patchStrategy(
            @NotNull @PathVariable Long id
    ) {
        try {
            if(strategyService.privateStrategy(id)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }
}