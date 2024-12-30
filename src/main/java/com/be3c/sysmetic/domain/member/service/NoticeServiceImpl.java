package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.Member;
import com.be3c.sysmetic.domain.member.entity.Notice;
import com.be3c.sysmetic.domain.member.entity.NoticeSearchType;
import com.be3c.sysmetic.domain.member.exception.MemberExceptionMessage;
import com.be3c.sysmetic.domain.member.exception.NoticeBadRequestException;
import com.be3c.sysmetic.domain.member.message.NoticeExceptionMessage;
import com.be3c.sysmetic.domain.member.repository.MemberRepository;
import com.be3c.sysmetic.domain.member.repository.NoticeRepository;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.util.SecurityUtils;
import com.be3c.sysmetic.global.util.file.dto.FileReferenceType;
import com.be3c.sysmetic.global.util.file.dto.FileRequest;
import com.be3c.sysmetic.global.util.file.dto.FileWithInfoResponse;
import com.be3c.sysmetic.global.util.file.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    private final SecurityUtils securityUtils;

    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private final FileService fileService;

    private final Integer PAGE_SIZE = 10; // 한 페이지 크기

    // 등록
    @Override
    @Transactional
    public boolean registerNotice(NoticeSaveRequestDto noticeSaveRequestDto,
                                  List<MultipartFile> fileList, List<MultipartFile> imageList) {

        if(fileList != null && fileList.size() > 3) {
            throw new NoticeBadRequestException(NoticeExceptionMessage.FILE_NUMBER_EXCEEDED.getMessage());
        }

        if(imageList != null && imageList.size() > 5) {
            throw new NoticeBadRequestException(NoticeExceptionMessage.FILE_NUMBER_EXCEEDED.getMessage());
        }

        Long writerId = securityUtils.getUserIdInSecurityContext();

        Member writer = memberRepository.findById(writerId).orElseThrow(() -> new EntityNotFoundException(MemberExceptionMessage.DATA_NOT_FOUND.getMessage()));

        Boolean fileExists = (fileList != null);
        Boolean imageExists = (imageList != null);

        Notice notice = Notice.createNotice(
                noticeSaveRequestDto.getNoticeTitle(),
                noticeSaveRequestDto.getNoticeContent(),
                writer, fileExists, imageExists,
                noticeSaveRequestDto.getIsOpen());

        noticeRepository.save(notice);

        if(fileExists) {
            for (MultipartFile file : fileList) {
                fileService.uploadAnyFile(file, new FileRequest(FileReferenceType.NOTICE_BOARD_FILE, notice.getId()));
            }
        }

        if(imageExists) {
            for (MultipartFile image : imageList) {
                fileService.uploadImage(image, new FileRequest(FileReferenceType.NOTICE_BOARD_IMAGE, notice.getId()));
            }
        }

        return true;
    }


    // 관리자 검색 조회
    // 검색 (사용: title, content, titlecontent, writer) (설명: 제목, 내용, 제목+내용, 작성자)
    @Override
    public PageResponse<NoticeAdminListOneShowResponseDto> findNoticeAdmin(NoticeSearchType searchType, String searchText, Integer page) {

        Page<Notice> noticeList = noticeRepository.adminNoticeSearchWithBooleanBuilder(searchType, searchText, PageRequest.of(page, PAGE_SIZE));

        List<NoticeAdminListOneShowResponseDto> noticeAdminDtoList = noticeList.stream()
                .map(notice -> NoticeAdminListOneShowResponseDto.builder()
                        .noticeId(notice.getId())
                        .noticeTitle(notice.getNoticeTitle())
                        .writerNickname(notice.getWriterNickname())
                        .writeDate(notice.getWriteDate())
                        .hits(notice.getHits())
                        .fileExist(notice.getFileExists())
                        .isOpen(notice.getIsOpen())
                        .build())
                .collect(Collectors.toList());

        return PageResponse.<NoticeAdminListOneShowResponseDto>builder()
                .currentPage(noticeList.getNumber())
                .pageSize(PAGE_SIZE)
                .totalElement(noticeList.getTotalElements())
                .totalPages(noticeList.getTotalPages())
                .content(noticeAdminDtoList)
                .build();
    }


    // 관리자 공지사항 목록 공개여부 수정
    @Override
    @Transactional
    public boolean modifyNoticeClosed(Long noticeId) {

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeExceptionMessage.NOT_FOUND_NOTICE.getMessage()));

        notice.setIsOpen(!notice.getIsOpen());

        return true;
    }


    // 관리자 공지사항 수정
    @Override
    @Transactional
    public boolean modifyNotice(Long noticeId, NoticeModifyRequestDto noticeModifyRequestDto,
                                List<MultipartFile> newFileList, List<MultipartFile> newImageList) {

        Long correctorId = securityUtils.getUserIdInSecurityContext();

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeExceptionMessage.NOT_FOUND_NOTICE.getMessage()));

        Boolean fileExists = modifyNoticeNewDelete(FileReferenceType.NOTICE_BOARD_FILE, noticeId, newFileList, noticeModifyRequestDto.getDeleteFileIdList());
        Boolean imageExists = modifyNoticeNewDelete(FileReferenceType.NOTICE_BOARD_IMAGE, noticeId, newImageList, noticeModifyRequestDto.getDeleteImageIdList());

        notice.setNoticeTitle(noticeModifyRequestDto.getNoticeTitle());
        notice.setNoticeContent(noticeModifyRequestDto.getNoticeContent());
        notice.setFileExists(fileExists);
        notice.setImageExists(imageExists);
        notice.setCorrectorId(correctorId);
        notice.setCorrectDate(LocalDateTime.now());
        notice.setIsOpen(noticeModifyRequestDto.getIsOpen());

        return true;
    }

    private boolean modifyNoticeNewDelete(FileReferenceType fileReferenceType, Long noticeId, List<MultipartFile> newFileList, List<Long> deleteFileIdList) {

        int exceedNumber;
        if (fileReferenceType.equals(FileReferenceType.NOTICE_BOARD_FILE)) {
            exceedNumber = 3;
        } else {
            exceedNumber = 5;
        }

        boolean fileExists;
        List<FileWithInfoResponse> nowFileDtoList = fileService.getFileWithInfosNullable(new FileRequest(fileReferenceType, noticeId));

        if (nowFileDtoList != null) {

            List<Long> nowFileIdList = new ArrayList<>();
            for (FileWithInfoResponse file : nowFileDtoList) {
                nowFileIdList.add(file.id());
            }
            int nowCountFile = nowFileDtoList.size();

            if(!(deleteFileIdList == null || deleteFileIdList.isEmpty())) {
                for (Long fileId : deleteFileIdList) {
                    if (nowFileIdList.contains(fileId)) {
                        fileService.deleteFileById(fileId);
                        nowCountFile--;
                    } else {
                        throw new EntityNotFoundException(NoticeExceptionMessage.NOT_FOUND_FILE.getMessage());
                    }
                }
            }
            if (newFileList != null) {
                nowCountFile = nowCountFile + newFileList.size();
                if (nowCountFile > exceedNumber) {
                    throw new NoticeBadRequestException(NoticeExceptionMessage.FILE_NUMBER_EXCEEDED.getMessage());
                }
            }

            fileExists = nowCountFile > 0;

        } else {
            fileExists = false;

            if (!(deleteFileIdList == null || deleteFileIdList.isEmpty())) {
                throw new EntityNotFoundException(NoticeExceptionMessage.NOT_FOUND_FILE.getMessage());
            }

            if (newFileList != null) {
                int newFileListSize = newFileList.size();
                if (newFileListSize > exceedNumber) {
                    throw new NoticeBadRequestException(NoticeExceptionMessage.FILE_NUMBER_EXCEEDED.getMessage());
                }
                fileExists = true;
            }
        }

        if (fileReferenceType.equals(FileReferenceType.NOTICE_BOARD_FILE)) {
            if(newFileList != null) {
                for (MultipartFile file : newFileList) {
                    fileService.uploadAnyFile(file, new FileRequest(fileReferenceType, noticeId));
                }
            }
        } else {
            if(newFileList != null) {
                for (MultipartFile file : newFileList) {
                    fileService.uploadImage(file, new FileRequest(fileReferenceType, noticeId));
                }
            }
        }

        return fileExists;
    }


    // 관리자 문의 삭제
    @Override
    @Transactional
    public boolean deleteAdminNotice(Long noticeId) {

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeExceptionMessage.NOT_FOUND_NOTICE.getMessage()));

        if (notice.getImageExists()) {
            fileService.deleteFiles(new FileRequest(FileReferenceType.NOTICE_BOARD_IMAGE, noticeId));
        }
        if (notice.getFileExists()) {
            fileService.deleteFiles(new FileRequest(FileReferenceType.NOTICE_BOARD_FILE, noticeId));
        }

        noticeRepository.delete(notice);

        return true;
    }


    // 관리자 공지사항 목록 삭제
    @Override
    @Transactional
    public Map<Long, String> deleteAdminNoticeList(NoticeListDeleteRequestDto noticeListDeleteRequestDto) {

        List<Long> noticeIdList = noticeListDeleteRequestDto.getNoticeIds();

        if (noticeIdList == null || noticeIdList.isEmpty()) {
            throw new IllegalArgumentException("공지가 한 개도 선택되지 않았습니다.");
        }

        Map<Long, String> failDelete = new HashMap<>();
        List<Long> successDelete = new ArrayList<>();

        for (Long noticeId : noticeIdList) {
            try {
                Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeExceptionMessage.NOT_FOUND_NOTICE.getMessage()));

                if (notice.getFileExists()) {
                    fileService.deleteFiles(new FileRequest(FileReferenceType.NOTICE_BOARD_FILE, noticeId));
                }
                if (notice.getImageExists()) {
                    fileService.deleteFiles(new FileRequest(FileReferenceType.NOTICE_BOARD_IMAGE, noticeId));
                }

                successDelete.add(noticeId);
            }
            catch (EntityNotFoundException e) {
                failDelete.put(noticeId, e.getMessage());
            }
        }

        noticeRepository.bulkDelete(successDelete);

        return failDelete;
    }


    // 일반 검색 조회
    // 검색 (조건: 제목+내용)
    @Override
    public PageResponse<NoticeListOneShowResponseDto> findNotice(String searchText, Integer page) {

        Page<Notice> noticeList = noticeRepository.noticeSearchWithBooleanBuilder(searchText, PageRequest.of(page, PAGE_SIZE));

        List<NoticeListOneShowResponseDto> noticeDtoList = noticeList.stream()
                .map(notice -> NoticeListOneShowResponseDto.builder()
                        .noticeId(notice.getId())
                        .noticeTitle(notice.getNoticeTitle())
                        .writeDate(notice.getWriteDate())
                        .fileExists(notice.getFileExists())
                        .build())
                .collect(Collectors.toList());


        return PageResponse.<NoticeListOneShowResponseDto>builder()
                .currentPage(noticeList.getNumber())
                .pageSize(PAGE_SIZE)
                .totalElement(noticeList.getTotalElements())
                .totalPages(noticeList.getTotalPages())
                .content(noticeDtoList)
                .build();
    }

    @Override
    public NoticeDetailAdminShowResponseDto getAdminNoticeDetail(Long noticeId, NoticeSearchType searchType, String searchText) {

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeExceptionMessage.NOT_FOUND_NOTICE.getMessage()));

        Notice previousNotice = noticeRepository.findPreviousNoticeAdmin(noticeId, searchType, searchText);
        NoticePreviousNextDto noticePreviousDto = getPreviousNextNoticeDto(previousNotice);

        Notice nextNotice = noticeRepository.findNextNoticeAdmin(noticeId, searchType, searchText);
        NoticePreviousNextDto noticeNextDto = getPreviousNextNoticeDto(nextNotice);

        List<NoticeDetailFileShowResponseDto> fileDtoList = getFileDtoList(notice);
        List<NoticeDetailImageShowResponseDto> imageDtoList = getImageDtoList(notice);

        return NoticeDetailAdminShowResponseDto.builder()
                .searchType(searchType.getParameter())
                .searchText(searchText)
                .noticeId(notice.getId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .writeDate(notice.getWriteDate())
                .correctDate(notice.getCorrectDate())
                .writerNickname(notice.getWriterNickname())
                .hits(notice.getHits())
                .fileExist(notice.getFileExists())
                .imageExist(notice.getImageExists())
                .isOpen(notice.getIsOpen())
                .fileDtoList(fileDtoList)
                .imageDtoList(imageDtoList)
                .previousId(noticePreviousDto.getNoticeId())
                .previousTitle(noticePreviousDto.getNoticeTitle())
                .previousWriteDate(noticePreviousDto.getNoticeWriteDate())
                .nextId(noticeNextDto.getNoticeId())
                .nextTitle(noticeNextDto.getNoticeTitle())
                .nextWriteDate(noticeNextDto.getNoticeWriteDate())
                .build();
    }

    private NoticePreviousNextDto getPreviousNextNoticeDto(Notice notice) {

        Long noticeId;
        String noticeTitle;
        LocalDateTime noticeWriteDate;

        if (notice == null) {
            noticeId = null;
            noticeTitle = null;
            noticeWriteDate = null;
        } else {
            noticeId = notice.getId();
            noticeTitle = notice.getNoticeTitle();
            noticeWriteDate = notice.getWriteDate();
        }

        return NoticePreviousNextDto.builder()
                .noticeId(noticeId)
                .noticeTitle(noticeTitle)
                .noticeWriteDate(noticeWriteDate)
                .build();
    }

    private List<NoticeDetailFileShowResponseDto> getFileDtoList(Notice notice) {

        List<NoticeDetailFileShowResponseDto> fileDtoList = null;
        if (notice.getFileExists()) {
            List<FileWithInfoResponse> fileList = fileService.getFileWithInfosNullable(new FileRequest(FileReferenceType.NOTICE_BOARD_FILE, notice.getId()));
            fileDtoList = new ArrayList<>();
            for (FileWithInfoResponse f : fileList) {
                NoticeDetailFileShowResponseDto noticeDetailFileShowResponseDto = NoticeDetailFileShowResponseDto.builder()
                        .fileId(f.id())
                        .fileSize(f.fileSize())
                        .originalName(f.originalName())
                        .path(f.url())
                        .build();
                fileDtoList.add(noticeDetailFileShowResponseDto);
            }
        }

        return fileDtoList;
    }

    private List<NoticeDetailImageShowResponseDto> getImageDtoList(Notice notice) {

        List<NoticeDetailImageShowResponseDto> imageDtoList = null;
        if (notice.getImageExists()) {
            List<FileWithInfoResponse> imageList = fileService.getFileWithInfosNullable(new FileRequest(FileReferenceType.NOTICE_BOARD_IMAGE, notice.getId()));
            imageDtoList = new ArrayList<>();
            for (FileWithInfoResponse f : imageList) {
                NoticeDetailImageShowResponseDto noticeDetailImageShowResponseDto = NoticeDetailImageShowResponseDto.builder()
                        .fileId(f.id())
                        .originalName(f.originalName())
                        .path(f.url())
                        .build();
                imageDtoList.add(noticeDetailImageShowResponseDto);
            }
        }

        return imageDtoList;
    }

    @Override
    public NoticeDetailShowResponseDto getNoticeDetail(Long noticeId, String searchText) {

        Notice notice = noticeRepository.findByIdAndAndIsOpen(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeExceptionMessage.NOT_FOUND_NOTICE.getMessage()));

        notice.increaseHits();

        Notice previousNotice = noticeRepository.findPreviousNotice(noticeId, searchText);
        NoticePreviousNextDto noticePreviousDto = getPreviousNextNoticeDto(previousNotice);

        Notice nextNotice = noticeRepository.findNextNotice(noticeId, searchText);
        NoticePreviousNextDto noticeNextDto = getPreviousNextNoticeDto(nextNotice);

        List<NoticeDetailFileShowResponseDto> fileDtoList = getFileDtoList(notice);
        List<NoticeDetailImageShowResponseDto> imageDtoList = getImageDtoList(notice);

        return NoticeDetailShowResponseDto.builder()
                .searchText(searchText)
                .noticeId(notice.getId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .writeDate(notice.getWriteDate())
                .fileDtoList(fileDtoList)
                .imageDtoList(imageDtoList)
                .previousId(noticePreviousDto.getNoticeId())
                .previousTitle(noticePreviousDto.getNoticeTitle())
                .previousWriteDate(noticePreviousDto.getNoticeWriteDate())
                .nextId(noticeNextDto.getNoticeId())
                .nextTitle(noticeNextDto.getNoticeTitle())
                .nextWriteDate(noticeNextDto.getNoticeWriteDate())
                .build();
    }

    @Override
    public NoticeShowModifyPageResponseDto getAdminNoticeModifyPage(Long noticeId) {

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeExceptionMessage.NOT_FOUND_NOTICE.getMessage()));

        List<NoticeDetailFileShowResponseDto> fileDtoList = getFileDtoList(notice);
        List<NoticeDetailImageShowResponseDto> imageDtoList = getImageDtoList(notice);

        return NoticeShowModifyPageResponseDto.builder()
                .noticeId(notice.getId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .fileExist(notice.getFileExists())
                .imageExist(notice.getImageExists())
                .isOpen(notice.getIsOpen())
                .fileDtoList(fileDtoList)
                .imageDtoList(imageDtoList)
                .build();
    }
}
