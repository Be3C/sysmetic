package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.Inquiry;
import com.be3c.sysmetic.domain.member.entity.Notice;
import com.be3c.sysmetic.global.util.file.dto.FileReferenceType;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface NoticeService {

    // 등록
    boolean registerNotice(Long writerId, String noticeTitle, String noticeContent, Boolean isOpen,
                           List<MultipartFile> fileLists, List<MultipartFile> imageList);

    // 관리자 검색 조회
    // 검색 (사용: title, content, titlecontent, writer) (설명: 제목, 내용, 제목+내용, 작성자)
    Page<Notice> findNoticeAdmin(String searchType, String searchText, Integer page);

    // 관리자 공지사항 목록 공개여부 수정
    boolean modifyNoticeClosed(Long noticeId);

    // 공지사항 조회 후 조회수 상승
    void upHits(Long noticeId);

    // 관리자 문의 수정
    boolean modifyNotice(Long noticeId, String noticeTitle, String noticeContent, Long correctorId, Boolean isOpen,
                         List<Long> deleteFileIdList, List<Long> deleteImageIdList,
                         List<MultipartFile> newFileList, List<MultipartFile> newImageList);

    boolean modifyNoticeNewDelete(FileReferenceType fileReferenceType, Long noticeId, List<MultipartFile> newFileList, List<Long> deleteFileIdList);

    // 관리자 문의 삭제
    boolean deleteAdminNotice(Long noticeId);

    // 관리자 문의 목록 삭제
    Map<Long, String> deleteAdminNoticeList(List<Long> noticeIdList);

    // 일반 검색 조회
    // 검색 (조건: 제목+내용)
    Page<Notice> findNotice(String searchText, Integer page);

    NoticeAdminListOneShowResponseDto noticeToNoticeAdminListOneShowResponseDto(Notice notice);

    NoticeDetailAdminShowResponseDto noticeIdToNoticeDetailAdminShowResponseDto(Long noticeId, String searchType, String searchText);

    NoticeDetailShowResponseDto noticeIdToticeDetailShowResponseDto(Long noticeId, String searchText);

    NoticeShowModifyPageResponseDto noticeIdTonoticeShowModifyPageResponseDto(Long noticeId);
}
