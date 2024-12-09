package com.be3c.sysmetic.domain.member.controller;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.Notice;
import com.be3c.sysmetic.domain.member.service.NoticeService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.common.response.SuccessCode;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1")
public class NoticeContoller implements NoticeControllerDocs {

    private final NoticeService noticeService;

    private final Integer pageSize = 10; // 한 페이지 크기

    /*
        관리자 공지사항 등록 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 공지사항이 등록에 성공했을 때 : OK
        3. 공지사항이 등록에 실패했을 때 : INTERNAL_SERVER_ERROR
        4. 데이터의 형식이 올바르지 않음 : BAD_REQUEST
        5. 등록하는 관리자 정보를 찾지 못했을 때 : NOT_FOUND
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/admin/notice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<Long>> saveAdminNotice(
            @RequestPart(value = "NoticeSaveRequestDto") @Valid NoticeSaveRequestDto noticeSaveRequestDto,
            @RequestPart(value = "fileList", required = false) List<MultipartFile> fileList,
            @RequestPart(value = "imageList", required = false) List<MultipartFile> imageList) {

        if(fileList != null && fileList.size() > 3) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "등록하려는 파일이 3개 초과입니다."));
        }

        if(imageList != null && imageList.size() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "등록하려는 이미지가 5개 초과입니다."));
        }

        try {

            if (noticeService.registerNotice(
                    noticeSaveRequestDto,
                    fileList,
                    imageList)) {

                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }


    /*
        관리자 공지사항 조회 / 검색 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 공지사항 데이터 조회에 성공했을 때 : OK
        3. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/notice")
    public ResponseEntity<APIResponse<PageResponse<NoticeAdminListOneShowResponseDto>>> showAdminNotice(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "searchType", required = false, defaultValue = "title") String searchType,
            @RequestParam(value = "searchText", required = false) String searchText) {

        if (page < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "페이지가 0보다 작습니다"));
        }

        if (!(searchType.equals("title") || searchType.equals("content") || searchType.equals("titlecontent") || searchType.equals("writer"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "쿼리 파라미터 searchType이 올바르지 않습니다."));
        }

        PageResponse<NoticeAdminListOneShowResponseDto> adminNoticePage = noticeService.findNoticeAdmin(searchType, searchText, page);

        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success(adminNoticePage));
    }


    /*
        관리자 공지사항 공개여부 수정 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 공개여부 수정에 성공했을 때 : OK
        3. 공개여부 수정에 실패했을 때 : INTERNAL_SERVER_ERROR
        4. 해당 공지사항을 찾지 못했을 때 : NOT_FOUND
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
    @PutMapping("/admin/notice/{noticeId}/open-close")
    public ResponseEntity<APIResponse<Long>> modifyNoticeClosed(
            @PathVariable(name="noticeId") Long noticeId) {

        try {

            if (noticeService.modifyNoticeClosed(noticeId)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }


    /*
        관리자 공지사항 상세 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 공지사항의 상세 데이터 조회에 성공했을 때 : OK
        3. 해당 공지사항을 찾지 못했을 때 : NOT_FOUND
        4. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/notice/{noticeId}")
    public ResponseEntity<APIResponse<NoticeDetailAdminShowResponseDto>> showAdminNoticeDetail(
            @PathVariable(name="noticeId") Long noticeId,
            @RequestParam(value = "searchType", required = false, defaultValue = "title") String searchType,
            @RequestParam(value = "searchText", required = false) String searchText) {

        if (!(searchType.equals("title") || searchType.equals("content") || searchType.equals("titlecontent") || searchType.equals("writer"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "쿼리 파라미터 searchType이 올바르지 않습니다."));
        }

        try {

            NoticeDetailAdminShowResponseDto noticeDetailAdminShowResponseDto = noticeService.noticeIdToNoticeDetailAdminShowResponseDto(noticeId, searchType, searchText);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(noticeDetailAdminShowResponseDto));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }


    /*
        관리자 공지사항 수정 화면 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 공지사항 수정 화면 조회에 성공했을 때 : OK
        3. 공지사항 수정 화면 조회에 실패했을 때 : NOT_FOUND
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/notice/{noticeId}/modify")
    public ResponseEntity<APIResponse<NoticeShowModifyPageResponseDto>> showModifyAdminNotice(
            @PathVariable(name="noticeId") Long noticeId) {

        try {

            NoticeShowModifyPageResponseDto noticeShowModifyPageResponseDto = noticeService.noticeIdTonoticeShowModifyPageResponseDto(noticeId);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(noticeShowModifyPageResponseDto));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }


    /*
        관리자 공지사항 수정 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 공지사항 수정에 성공했을 때 : OK
        3. 공지사항 수정에 실패했을 때 : INTERNAL_SERVER_ERROR
        4. 해당 공지사항을 찾지 못했을 때 : NOT_FOUND
        5. 데이터의 형식이 올바르지 않음 : BAD_REQUEST
            +) 공지사항 수정 화면에 들어온 시간이 해당 공지사항 최종수정일시보다 작음
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/admin/notice/{noticeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<Long>> modifyAdminNotice(
            @PathVariable(name="noticeId") Long noticeId,
            @RequestPart(value = "NoticeModifyRequestDto") @Valid NoticeModifyRequestDto noticeModifyRequestDto,
            @RequestPart(value = "newFileList", required = false) List<MultipartFile> newFileList,
            @RequestPart(value = "newImageList", required = false) List<MultipartFile> newImageList) {

//        LocalDateTime modifyInModifyPageTime = noticeModifyRequestDto.getModifyInModifyPageTime();
//        if (modifyInModifyPageTime == null) {
//            String str = "3000-11-05 13:47:13.248";
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
//            modifyInModifyPageTime = LocalDateTime.parse(str, formatter);
//        }

        try {

            if (noticeService.modifyNotice(
                    noticeId,
                    noticeModifyRequestDto,
                    newFileList,
                    newImageList)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));

//            if (modifyInModifyPageTime.isBefore(notice.getCorrectDate())) {
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "공지사항 수정 화면에 들어온 시간이 해당 공지사항 최종수정일시보다 작습니다."));
//            }
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }


    /*
        관리자 공지사항 삭제 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 공지사항 삭제에 성공했을 때 : OK
        3. 공지사항 삭제에 실패했을 때 : INTERNAL_SERVER_ERROR
        4. 해당 공지사항을 찾지 못했을 때 : NOT_FOUND
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin/notice/{noticeId}")
    public ResponseEntity<APIResponse<Long>> deleteAdminNotice(
            @PathVariable(name="noticeId") Long noticeId) {

        try {
            if (noticeService.deleteAdminNotice(noticeId)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }


    /*
        관리자 공지사항 목록 삭제 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 공지사항 목록 삭제에 성공했을 때 : OK
        3. 해당 공지사항을 찾지 못했을 때 : NOT_FOUND
        4. 공지사항 중 삭제에 실패했을 때 : MULTI_STATUS
        5. 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
//    @PreAuthorize("hasRole('ROLE_USER_MANAGER') or hasRole('ROLE_TRADER_MANAGER') or hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin/notice")
    public ResponseEntity<APIResponse<Map<Long, String>>> deleteAdminNoticeList(
            @RequestBody @Valid NoticeListDeleteRequestDto noticeListDeleteRequestDto) {

        try {
            Map<Long, String> deleteResult = noticeService.deleteAdminNoticeList(noticeListDeleteRequestDto.getNoticeIds());

            if (deleteResult.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(APIResponse.success());
            }
            return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                    .body(APIResponse.success(SuccessCode.OK, deleteResult));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
        }
    }


    /*
        공지사항 조회 / 검색 API
        1. 공지사항 데이터 조회에 성공했을 때 : OK
        2. 파라미터 데이터의 형식이 올바르지 않음 : BAD_REQUEST
     */
    @Override
    @GetMapping("/notice")
    public ResponseEntity<APIResponse<PageResponse<NoticeListOneShowResponseDto>>> showNotice(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "searchText", required = false) String searchText) {

        if (page < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST, "페이지가 0보다 작습니다"));
        }

        PageResponse<NoticeListOneShowResponseDto> adminNoticePage = noticeService.findNotice(searchText, page);

        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success(adminNoticePage));
    }


    /*
        공지사항 상세 조회 API
        1. 사용자 인증 정보가 없음 : FORBIDDEN
        2. 공지사항의 상세 데이터 조회에 성공했을 때 : OK
        3. 공지사항의 상세 데이터 조회에 실패했을 때 : NOT_FOUND
     */
    @Override
    @GetMapping("/notice/{noticeId}")
    public ResponseEntity<APIResponse<NoticeDetailShowResponseDto>> showNoticeDetail(
            @PathVariable(name="noticeId") Long noticeId,
            @RequestParam(value = "searchText", required = false) String searchText) {

        try {
            noticeService.upHits(noticeId);

            NoticeDetailShowResponseDto noticeDetailShowResponseDto = noticeService.noticeIdToticeDetailShowResponseDto(noticeId, searchText);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(noticeDetailShowResponseDto));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }
}