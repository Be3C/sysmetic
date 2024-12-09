package com.be3c.sysmetic.domain.member.controller;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.strategy.dto.StrategyStatisticsGetResponseDto;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "공지사항 API", description = "관리자, 트레이더, 투자자 공지사항 API")
public interface NoticeControllerDocs {

    // 관리자 공지사항 등록 API
    @Operation(
            summary = "관리자 공지사항 등록",
            description = "관리자가 새로운 공지사항을 등록하는 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "사용자 인증 정보가 없음 (FORBIDDEN)"
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 등록 성공 (OK)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "공지사항 등록 실패 (INTERNAL_SERVER_ERROR)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "데이터의 형식이 올바르지 않음 (BAD_REQUEST)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "등록하는 관리자 정보를 찾지 못함 (NOT_FOUND)"
            )
    })
    ResponseEntity<APIResponse<Long>> saveAdminNotice(
            @RequestPart @Valid NoticeSaveRequestDto noticeSaveRequestDto,
            @RequestPart(value = "fileList", required = false) List<MultipartFile> fileList,
            @RequestPart(value = "imageList", required = false) List<MultipartFile> imageList);


    // 관리자 공지사항 조회 / 검색 API
    @Operation(
            summary = "관리자 공지사항 조회/검색",
            description = "관리자가 공지사항을 조회하거나 검색하는 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "사용자 인증 정보가 없음 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 데이터 조회 성공 (OK)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "파라미터 데이터의 형식이 올바르지 않음 (BAD_REQUEST)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            )
    })
    @Parameters({
            @Parameter(name = "searchType", description = "검색 유형 (사용: title, content, titlecontent, writer) (설명: 제목, 내용, 제목+내용, 작성자)"),
            @Parameter(name = "searchText", description = "검색 텍스트")
    })
    ResponseEntity<APIResponse<PageResponse<NoticeAdminListOneShowResponseDto>>> showAdminNotice(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "searchType", required = false, defaultValue = "title") String searchType,
            @RequestParam(value = "searchText", required = false) String searchText);


    // 관리자 공지사항 공개여부 수정 API
    @Operation(
            summary = "관리자 공지사항 개별 공개여부 수정",
            description = "관리자가 개별 공지사항의 공개여부를 수정하는 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "사용자 인증 정보가 없음 (FORBIDDEN)"
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "공개여부 수정 성공 (OK)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "공개여부 수정 실패 (INTERNAL_SERVER_ERROR)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 공지사항을 찾지 못함 (NOT_FOUND)"
            )
    })
    ResponseEntity<APIResponse<Long>> modifyNoticeClosed(
            @PathVariable Long noticeId);


    // 관리자 공지사항 상세 조회 API
    @Operation(
            summary = "관리자 공지사항 상세 조회",
            description = "관리자가 특정 공지사항의 상세 데이터를 조회하는 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "사용자 인증 정보가 없음 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 상세 데이터 조회 성공 (OK)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 공지사항을 찾지 못함 (NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "파라미터 데이터의 형식이 올바르지 않음 (BAD_REQUEST)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            )
    })
    @Parameters({
            @Parameter(name = "searchType", description = "검색 유형 (사용: title, content, titlecontent, writer) (설명: 제목, 내용, 제목+내용, 작성자)"),
            @Parameter(name = "searchText", description = "검색 텍스트")
    })
    ResponseEntity<APIResponse<NoticeDetailAdminShowResponseDto>> showAdminNoticeDetail(
            @PathVariable(name="noticeId") Long noticeId,
            @RequestParam(value = "searchType", required = false, defaultValue = "title") String searchType,
            @RequestParam(value = "searchText", required = false) String searchText);


    // 관리자 공지사항 수정 화면 조회 API
    @Operation(
            summary = "관리자 공지사항 수정 화면 조회",
            description = "관리자가 특정 공지사항 수정 화면 데이터를 조회하는 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "사용자 인증 정보가 없음 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 수정 화면 데이터 조회 성공 (OK)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "공지사항 수정 화면 데이터 조회 실패 (NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            )
    })
    ResponseEntity<APIResponse<NoticeShowModifyPageResponseDto>> showModifyAdminNotice(
            @PathVariable(name="noticeId") Long noticeId);


    // 관리자 공지사항 수정 API
    @Operation(
            summary = "관리자 공지사항 수정",
            description = "관리자가 기존 공지사항을 수정하는 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "사용자 인증 정보가 없음 (FORBIDDEN)"
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 수정 성공 (OK)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "공지사항 수정 실패 (INTERNAL_SERVER_ERROR)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 공지사항을 찾지 못함 (NOT_FOUND)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "데이터의 형식이 올바르지 않음 (BAD_REQUEST)\n+ +) 공지사항 수정 화면에 들어온 시간이 해당 공지사항 최종수정일시보다 작음"
            )
    })
    ResponseEntity<APIResponse<Long>> modifyAdminNotice(
            @PathVariable(name="noticeId") Long noticeId,
            @RequestPart(value = "NoticeModifyRequestDto") @Valid NoticeModifyRequestDto noticeModifyRequestDto,
            @RequestPart(value = "newFileList", required = false) List<MultipartFile> newFileList,
            @RequestPart(value = "newImageList", required = false) List<MultipartFile> newImageList);


    // 관리자 공지사항 삭제 API
    @Operation(
            summary = "관리자 공지사항 삭제",
            description = "관리자가 특정 공지사항을 삭제하는 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "사용자 인증 정보가 없음 (FORBIDDEN)"
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 삭제 성공 (OK)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "공지사항 삭제 실패 (INTERNAL_SERVER_ERROR)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 공지사항을 찾지 못함 (NOT_FOUND)"
            )
    })
    ResponseEntity<APIResponse<Long>> deleteAdminNotice(
            @PathVariable(name="noticeId") Long noticeId);


    // 관리자 공지사항 목록 삭제 API
    @Operation(
            summary = "관리자 공지사항 목록 삭제",
            description = "관리자가 목록 화면에서 여러 공지사항을 한 번에 삭제하는 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "사용자 인증 정보가 없음 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 목록 삭제 성공 (OK)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 공지사항을 찾지 못함 (NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            ),
            @ApiResponse(
                    responseCode = "207",
                    description = "공지사항 중 삭제에 실패 (MULTI_STATUS)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "데이터의 형식이 올바르지 않음 (BAD_REQUEST)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            )
    })
    ResponseEntity<APIResponse<Map<Long, String>>> deleteAdminNoticeList(
            @RequestBody @Valid NoticeListDeleteRequestDto noticeListDeleteRequestDto);


    // 공지사항 조회 / 검색 API
    @Operation(
            summary = "공지사항 조회 및 검색",
            description = "공지사항을 조회하거나 검색하는 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 조회 성공 (OK)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "파라미터 데이터의 형식이 올바르지 않음 (BAD_REQUEST)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            )
    })
    @Parameters({
            @Parameter(name = "searchText", description = "검색 텍스트")
    })
    ResponseEntity<APIResponse<PageResponse<NoticeListOneShowResponseDto>>> showNotice(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "searchText", required = false) String searchText);


    // 공지사항 상세 조회 API
    @Operation(
            summary = "공지사항 상세 조회",
            description = "공지사항의 상세 데이터를 조회하는 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "사용자 인증 정보가 없음 (FORBIDDEN)"
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 상세 조회 성공 (OK)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "공지사항 상세 조회 실패 (NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))
            )
    })
    @Parameters({
            @Parameter(name = "searchText", description = "검색 텍스트")
    })
    ResponseEntity<APIResponse<NoticeDetailShowResponseDto>> showNoticeDetail(
            @PathVariable(name="noticeId") Long noticeId,
            @RequestParam(value = "searchText", required = false) String searchText);
}
