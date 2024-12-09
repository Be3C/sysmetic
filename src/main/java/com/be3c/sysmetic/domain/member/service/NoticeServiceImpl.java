package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.controller.NoticeContoller;
import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.Member;
import com.be3c.sysmetic.domain.member.entity.Notice;
import com.be3c.sysmetic.domain.member.exception.MemberExceptionMessage;
import com.be3c.sysmetic.domain.member.message.NoticeFailMessage;
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

    private final Integer pageSize = 10; // 한 페이지 크기

    // 등록
    @Override
    @Transactional
    public boolean registerNotice(NoticeSaveRequestDto noticeSaveRequestDto,
                                  List<MultipartFile> fileList, List<MultipartFile> imageList) {

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
    public PageResponse<NoticeAdminListOneShowResponseDto> findNoticeAdmin(String searchType, String searchText, Integer page) {

        Page<Notice> noticeList = noticeRepository.adminNoticeSearchWithBooleanBuilder(searchType, searchText, PageRequest.of(page, 10));

        List<NoticeAdminListOneShowResponseDto> noticeAdminDtoList = noticeList.stream()
                .map(this::noticeToNoticeAdminListOneShowResponseDto).collect(Collectors.toList());

        return PageResponse.<NoticeAdminListOneShowResponseDto>builder()
                .currentPage(page)
                .pageSize(pageSize)
                .totalElement(noticeList.getTotalElements())
                .totalPages(noticeList.getTotalPages())
                .content(noticeAdminDtoList)
                .build();
    }


    // 관리자 공지사항 목록 공개여부 수정
    @Override
    @Transactional
    public boolean modifyNoticeClosed(Long noticeId) {

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_NOTICE.getMessage()));

        if (!notice.getIsOpen()) {
            notice.setIsOpen(true);
        } else {
            notice.setIsOpen(false);
        }

        return true;
    }


    // 공지사항 조회 후 조회수 상승
    @Override
    @Transactional
    public void upHits(Long noticeId) {

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_NOTICE.getMessage()));

        notice.setHits(notice.getHits() + 1);
    }

    // 관리자 공지사항 수정
    @Override
    @Transactional
    public boolean modifyNotice(Long noticeId, NoticeModifyRequestDto noticeModifyRequestDto,
                                List<MultipartFile> newFileList, List<MultipartFile> newImageList) {

        Long correctorId = securityUtils.getUserIdInSecurityContext();

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_NOTICE.getMessage()));

        Boolean fileExists = modifyNoticeNewDelete(FileReferenceType.NOTICE_BOARD_FILE, noticeId, newFileList, noticeModifyRequestDto.getDeleteFileIdList());

        Boolean imageExists = modifyNoticeNewDelete(FileReferenceType.NOTICE_BOARD_IMAGE, noticeId, newImageList, noticeModifyRequestDto.getDeleteImageIdList());

        notice.setNoticeTitle(noticeModifyRequestDto.getNoticeTitle());
        notice.setNoticeContent(noticeModifyRequestDto.getNoticeContent());
        notice.setFileExists(fileExists);
        notice.setImageExists(imageExists);
        notice.setCorrectorId(correctorId);
        notice.setCorrectDate(LocalDateTime.now());
        notice.setIsOpen(noticeModifyRequestDto.getIsOpen());

        if(newFileList != null) {
            for (MultipartFile file : newFileList) {
                fileService.uploadAnyFile(file, new FileRequest(FileReferenceType.NOTICE_BOARD_FILE, notice.getId()));
            }
        }

        if(newImageList != null) {
            for (MultipartFile image : newImageList) {
                fileService.uploadImage(image, new FileRequest(FileReferenceType.NOTICE_BOARD_IMAGE, notice.getId()));
            }
        }

        return true;
    }

    @Override
    @Transactional
    public boolean modifyNoticeNewDelete(FileReferenceType fileReferenceType, Long noticeId, List<MultipartFile> newFileList, List<Long> deleteFileIdList) {

        boolean fileExists;
        List<FileWithInfoResponse> nowFileDtoList = fileService.getFileWithInfosNullable(new FileRequest(fileReferenceType, noticeId));

        if (nowFileDtoList != null) {
            fileExists = true;

            List<Long> nowFileIdList = new ArrayList<>();
            for (FileWithInfoResponse file : nowFileDtoList) {
                nowFileIdList.add(file.id());
            }
            int nowCountFile = nowFileDtoList.size();

            if (newFileList != null) {
                if (!(deleteFileIdList == null || deleteFileIdList.isEmpty())) {
                    for (Long fileId : deleteFileIdList) {
                        if (nowFileIdList.contains(fileId)) {
                            fileService.deleteFileById(fileId);
                            nowCountFile--;
                        } else {
                            if (fileReferenceType == FileReferenceType.NOTICE_BOARD_FILE) {
                                throw new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_FILE.getMessage());
                            } else if (fileReferenceType == FileReferenceType.NOTICE_BOARD_IMAGE) {
                                throw new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_IMAGE.getMessage());
                            }
                        }
                    }
                    int newFileListSize = newFileList.size();
                    nowCountFile = nowCountFile + newFileListSize;
                    if (nowCountFile > 3) {
                        if (fileReferenceType == FileReferenceType.NOTICE_BOARD_FILE) {
                            throw new EntityNotFoundException(NoticeFailMessage.FILE_NUMBER_EXCEEDED.getMessage());
                        } else if (fileReferenceType == FileReferenceType.NOTICE_BOARD_IMAGE) {
                            throw new EntityNotFoundException(NoticeFailMessage.IMAGE_NUMBER_EXCEEDED.getMessage());
                        }
                    }
                }
                else {
                    int newFileListSize = newFileList.size();
                    nowCountFile = nowCountFile + newFileListSize;
                    if (nowCountFile > 3) {
                        if (fileReferenceType == FileReferenceType.NOTICE_BOARD_FILE) {
                            throw new EntityNotFoundException(NoticeFailMessage.FILE_NUMBER_EXCEEDED.getMessage());
                        } else if (fileReferenceType == FileReferenceType.NOTICE_BOARD_IMAGE) {
                            throw new EntityNotFoundException(NoticeFailMessage.IMAGE_NUMBER_EXCEEDED.getMessage());
                        }
                    }
                }
            } else {
                if (!(deleteFileIdList == null || deleteFileIdList.isEmpty())) {
                    for (Long fileId : deleteFileIdList) {
                        if (nowFileIdList.contains(fileId)) {
                            fileService.deleteFileById(fileId);
                            nowCountFile--;
                        } else {
                            if (fileReferenceType == FileReferenceType.NOTICE_BOARD_FILE) {
                                throw new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_FILE.getMessage());
                            } else if (fileReferenceType == FileReferenceType.NOTICE_BOARD_IMAGE) {
                                throw new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_IMAGE.getMessage());
                            }
                        }
                    }
                    fileExists = nowCountFile > 0;
                }
            }
        } else {
            fileExists = false;

            if (newFileList != null) {
                if (!(deleteFileIdList == null || deleteFileIdList.isEmpty())) {
                    if (fileReferenceType == FileReferenceType.NOTICE_BOARD_FILE) {
                        throw new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_FILE.getMessage());
                    } else if (fileReferenceType == FileReferenceType.NOTICE_BOARD_IMAGE) {
                        throw new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_IMAGE.getMessage());
                    }
                } else {
                    int newFileListSize = newFileList.size();
                    if (newFileListSize > 3) {
                        if (fileReferenceType == FileReferenceType.NOTICE_BOARD_FILE) {
                            throw new EntityNotFoundException(NoticeFailMessage.FILE_NUMBER_EXCEEDED.getMessage());
                        } else if (fileReferenceType == FileReferenceType.NOTICE_BOARD_IMAGE) {
                            throw new EntityNotFoundException(NoticeFailMessage.IMAGE_NUMBER_EXCEEDED.getMessage());
                        }
                    }
                    fileExists = true;
                }
            } else {
                if (!(deleteFileIdList == null || deleteFileIdList.isEmpty())) {
                    if (fileReferenceType == FileReferenceType.NOTICE_BOARD_FILE) {
                        throw new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_FILE.getMessage());
                    } else if (fileReferenceType == FileReferenceType.NOTICE_BOARD_IMAGE) {
                        throw new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_IMAGE.getMessage());
                    }
                }
            }
        }

        return fileExists;
    }


    // 관리자 문의 삭제
    @Override
    @Transactional
    public boolean deleteAdminNotice(Long noticeId) {

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_NOTICE.getMessage()));

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
    public Map<Long, String> deleteAdminNoticeList(List<Long> noticeIdList) {

        if (noticeIdList == null || noticeIdList.isEmpty()) {
            throw new IllegalArgumentException("공지가 한 개도 선택되지 않았습니다.");
        }

        Map<Long, String> failDelete = new HashMap<>();

        for (Long noticeId : noticeIdList) {
            try {
                Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_NOTICE.getMessage()));

                if (notice.getFileExists()) {
                    fileService.deleteFiles(new FileRequest(FileReferenceType.NOTICE_BOARD_FILE, noticeId));
                }
                if (notice.getImageExists()) {
                    fileService.deleteFiles(new FileRequest(FileReferenceType.NOTICE_BOARD_IMAGE, noticeId));
                }
            }
            catch (EntityNotFoundException e) {
                failDelete.put(noticeId, e.getMessage());
            }
        }

        noticeRepository.bulkDelete(noticeIdList);

        return failDelete;
    }


    // 일반 검색 조회
    // 검색 (조건: 제목+내용)
    @Override
    public PageResponse<NoticeListOneShowResponseDto> findNotice(String searchText, Integer page) {

        Page<Notice> noticeList = noticeRepository.noticeSearchWithBooleanBuilder(searchText, PageRequest.of(page, 10));

        List<NoticeListOneShowResponseDto> noticeDtoList = noticeList.stream()
                .map(this::noticeToNoticeListOneShowResponseDto).collect(Collectors.toList());

        return PageResponse.<NoticeListOneShowResponseDto>builder()
                .currentPage(page)
                .pageSize(pageSize)
                .totalElement(noticeList.getTotalElements())
                .totalPages(noticeList.getTotalPages())
                .content(noticeDtoList)
                .build();
    }

    @Override
    public NoticeListOneShowResponseDto noticeToNoticeListOneShowResponseDto(Notice notice) {

        return NoticeListOneShowResponseDto.builder()
                .noticeId(notice.getId())
                .noticeTitle(notice.getNoticeTitle())
                .writeDate(notice.getWriteDate())
                .fileExists(notice.getFileExists())
                .build();
    }

    @Override
    public NoticeAdminListOneShowResponseDto noticeToNoticeAdminListOneShowResponseDto(Notice notice) {

        return NoticeAdminListOneShowResponseDto.builder()
                .noticeId(notice.getId())
                .noticeTitle(notice.getNoticeTitle())
                .writerNickname(notice.getWriterNickname())
                .writeDate(notice.getWriteDate())
                .hits(notice.getHits())
                .fileExist(notice.getFileExists())
                .isOpen(notice.getIsOpen())
                .build();
    }

    @Override
    public NoticeDetailAdminShowResponseDto noticeIdToNoticeDetailAdminShowResponseDto(Long noticeId, String searchType, String searchText) {

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_NOTICE.getMessage()));

        Optional<Notice> previousNoticeOptional = noticeRepository.findPreviousNoticeAdmin(noticeId, searchType, searchText);
        Long previousNoticeId;
        String previousNoticeTitle;
        LocalDateTime previousNoticeWriteDate;
        if (previousNoticeOptional.isEmpty()) {
            previousNoticeId = null;
            previousNoticeTitle = null;
            previousNoticeWriteDate = null;
        } else {
            previousNoticeId = previousNoticeOptional.orElse(null).getId();
            previousNoticeTitle = previousNoticeOptional.orElse(null).getNoticeTitle();
            previousNoticeWriteDate = previousNoticeOptional.orElse(null).getWriteDate();
        }

        Optional<Notice> nextNoticeOptional = noticeRepository.findNextNoticeAdmin(noticeId, searchType, searchText);
        Long nextNoticeId;
        String nextNoticeTitle;
        LocalDateTime nextNoticeWriteDate;
        if (nextNoticeOptional.isEmpty()) {
            nextNoticeId = null;
            nextNoticeTitle = null;
            nextNoticeWriteDate = null;
        } else {
            nextNoticeId = nextNoticeOptional.orElse(null).getId();
            nextNoticeTitle = nextNoticeOptional.orElse(null).getNoticeTitle();
            nextNoticeWriteDate = nextNoticeOptional.orElse(null).getWriteDate();
        }

        List<FileWithInfoResponse> fileList = fileService.getFileWithInfosNullable(new FileRequest(FileReferenceType.NOTICE_BOARD_FILE, notice.getId()));
        List<NoticeDetailFileShowResponseDto> fileDtoList;
        if (fileList == null) {
            fileDtoList = null;
        } else {
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

        List<FileWithInfoResponse> imageList = fileService.getFileWithInfosNullable(new FileRequest(FileReferenceType.NOTICE_BOARD_IMAGE, notice.getId()));
        List<NoticeDetailImageShowResponseDto> imageDtoList;
        if (imageList == null) {
            imageDtoList = null;
        } else {
            imageDtoList = new ArrayList<>();
            for (FileWithInfoResponse f : imageList) {
                NoticeDetailImageShowResponseDto noticeDetailImageShowResponseDto = NoticeDetailImageShowResponseDto.builder()
                        .fileId(f.id())
                        .path(f.url())
                        .build();
                imageDtoList.add(noticeDetailImageShowResponseDto);
            }
        }

        return NoticeDetailAdminShowResponseDto.builder()
                .searchType(searchType)
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
                .previousId(previousNoticeId)
                .previousTitle(previousNoticeTitle)
                .previousWriteDate(previousNoticeWriteDate)
                .nextId(nextNoticeId)
                .nextTitle(nextNoticeTitle)
                .nextWriteDate(nextNoticeWriteDate)
                .build();
    }

    @Override
    public NoticeDetailShowResponseDto noticeIdToticeDetailShowResponseDto(Long noticeId, String searchText) {

        Notice notice = noticeRepository.findByIdAndAndIsOpen(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_NOTICE.getMessage()));

        Optional<Notice> previousNoticeOptional = noticeRepository.findPreviousNotice(noticeId, searchText);
        Long previousNoticeId;
        String previousNoticeTitle;
        LocalDateTime previousNoticeWriteDate;
        if (previousNoticeOptional.isEmpty()) {
            previousNoticeId = null;
            previousNoticeTitle = null;
            previousNoticeWriteDate = null;
        } else {
            previousNoticeId = previousNoticeOptional.orElse(null).getId();
            previousNoticeTitle = previousNoticeOptional.orElse(null).getNoticeTitle();
            previousNoticeWriteDate = previousNoticeOptional.orElse(null).getWriteDate();
        }

        Optional<Notice> nextNoticeOptional = noticeRepository.findNextNotice(noticeId, searchText);
        Long nextNoticeId;
        String nextNoticeTitle;
        LocalDateTime nextNoticeWriteDate;
        if (nextNoticeOptional.isEmpty()) {
            nextNoticeId = null;
            nextNoticeTitle = null;
            nextNoticeWriteDate = null;
        } else {
            nextNoticeId = nextNoticeOptional.orElse(null).getId();
            nextNoticeTitle = nextNoticeOptional.orElse(null).getNoticeTitle();
            nextNoticeWriteDate = nextNoticeOptional.orElse(null).getWriteDate();
        }

        List<FileWithInfoResponse> fileList = fileService.getFileWithInfosNullable(new FileRequest(FileReferenceType.NOTICE_BOARD_FILE, notice.getId()));
        List<NoticeDetailFileShowResponseDto> fileDtoList;
        if (fileList == null) {
            fileDtoList = null;
        } else {
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

        List<FileWithInfoResponse> imageList = fileService.getFileWithInfosNullable(new FileRequest(FileReferenceType.NOTICE_BOARD_IMAGE, notice.getId()));
        List<NoticeDetailImageShowResponseDto> imageDtoList;
        if (imageList == null) {
            imageDtoList = null;
        } else {
            imageDtoList = new ArrayList<>();
            for (FileWithInfoResponse f : imageList) {
                NoticeDetailImageShowResponseDto noticeDetailImageShowResponseDto = NoticeDetailImageShowResponseDto.builder()
                        .fileId(f.id())
                        .path(f.url())
                        .build();
                imageDtoList.add(noticeDetailImageShowResponseDto);
            }
        }

        return NoticeDetailShowResponseDto.builder()
                .searchText(searchText)
                .noticeId(notice.getId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .writeDate(notice.getWriteDate())
                .fileDtoList(fileDtoList)
                .imageDtoList(imageDtoList)
                .previousId(previousNoticeId)
                .previousTitle(previousNoticeTitle)
                .previousWriteDate(previousNoticeWriteDate)
                .nextId(nextNoticeId)
                .nextTitle(nextNoticeTitle)
                .nextWriteDate(nextNoticeWriteDate)
                .build();
    }

    @Override
    public NoticeShowModifyPageResponseDto noticeIdTonoticeShowModifyPageResponseDto(Long noticeId) {

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new EntityNotFoundException(NoticeFailMessage.NOT_FOUND_NOTICE.getMessage()));

        List<FileWithInfoResponse> fileList = fileService.getFileWithInfosNullable(new FileRequest(FileReferenceType.NOTICE_BOARD_FILE, notice.getId()));
        List<NoticeDetailFileShowResponseDto> fileDtoList;
        if (fileList == null) {
            fileDtoList = null;
        } else {
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

        List<FileWithInfoResponse> imageList = fileService.getFileWithInfosNullable(new FileRequest(FileReferenceType.NOTICE_BOARD_IMAGE, notice.getId()));
        List<NoticeDetailImageShowResponseDto> imageDtoList;
        if (imageList == null) {
            imageDtoList = null;
        } else {
            imageDtoList = new ArrayList<>();
            for (FileWithInfoResponse f : imageList) {
                NoticeDetailImageShowResponseDto noticeDetailImageShowResponseDto = NoticeDetailImageShowResponseDto.builder()
                        .fileId(f.id())
                        .path(f.url())
                        .build();
                imageDtoList.add(noticeDetailImageShowResponseDto);
            }
        }

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
