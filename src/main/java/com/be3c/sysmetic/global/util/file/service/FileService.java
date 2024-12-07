package com.be3c.sysmetic.global.util.file.service;

import com.be3c.sysmetic.global.util.file.dto.FileRequest;
import com.be3c.sysmetic.global.util.file.dto.FileWithInfoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    /**
     * 이미지 업로드
     * @param file 업로드 할 이미지 파일
     * @param fileRequest ReferenceType과 ReferenceId 입력
     */
    void uploadImage(MultipartFile file, FileRequest fileRequest);
    /**
     * PDF 업로드
     * @param file 업로드 할 PDF 파일
     * @param fileRequest ReferenceType과 ReferenceId 입력
     */

    void uploadPdf(MultipartFile file, FileRequest fileRequest);
    /**
     * 확장자 무제한 업로드
     * @param file 업로드 할 파일
     * @param fileRequest ReferenceType과 ReferenceId 입력
     */
    void uploadAnyFile(MultipartFile file, FileRequest fileRequest);



    /**
     * presignedUrl 제공
     * @param fileRequest ReferenceType(참조 테이블명)과 ReferenceId(참조 id) 입력
     * @return 파일 url
     */
    String getFilePath(FileRequest fileRequest);

    /**
     * presignedUrl 혹은 null 반환
     * @param fileRequest ReferenceType(참조 테이블명)과 ReferenceId(참조 id) 입력
     * @return 파일 url 혹은 null
     */
    String getFilePathNullable(FileRequest fileRequest);

    /**
     * presignedUrl 리스트 제공
     * @param fileRequest ReferenceType(참조 테이블명)과 ReferenceId(참조 id) 입력
     * @return 파일 url 리스트
     */
    List<String> getFilePaths(FileRequest fileRequest);

    /**
     * presignedUrl과 파일 정보 한 개를 제공하거나 null 제공
     * @param fileRequest ReferenceType(참조 테이블명)과 ReferenceId(참조 id) 입력
     * @return 파일 url과 파일 메타 정보 1개 혹은 null
     */
    FileWithInfoResponse getFileWithInfoNullable(FileRequest fileRequest);

    /**
     * presignedUrl과 파일 정보 리스트를 제공
     * @param fileRequest ReferenceType(참조 테이블명)과 ReferenceId(참조 id) 입력
     * @return 파일 url과 파일 메타 정보 리스트
     */
    List<FileWithInfoResponse> getFileWithInfos(FileRequest fileRequest);

    /**
     * presignedUrl과 파일 정보 리스트를 제공하거나 null 제공
     * @param fileRequest ReferenceType(참조 테이블명)과 ReferenceId(참조 id) 입력
     * @return 파일 url과 파일 메타 정보 리스트 혹은 null
     */
    List<FileWithInfoResponse> getFileWithInfosNullable(FileRequest fileRequest);


    /**
     * 이미지 수정
     * @param file 수정된 파일
     * @param fileRequest 수정할 위치: ReferenceType, ReferenceId
     */
    void updateImage(MultipartFile file, FileRequest fileRequest);
    /**
     * pdf 수정
     * @param file 수정된 파일
     * @param fileRequest 수정할 위치: ReferenceType, ReferenceId
     */
    void updatePdf(MultipartFile file, FileRequest fileRequest);

    /**
     * 모든 형태의 파일 수정
     * @param file 수정된 파일
     * @param fileRequest 수정할 위치: ReferenceType, ReferenceId
     */
    void updateAnyFile(MultipartFile file, FileRequest fileRequest);


    /**
     * 수정할 위치에 여러 개의 이미지가 있는 경우
     * @param file 수정된 파일
     * @param updateTargetId 수정할 파일id
     */
    void updateImageById(MultipartFile file, Long updateTargetId);

    /**
     * 수정할 위치에 여러 개의 pdf가 있는 경우
     * @param file 수정된 파일
     * @param updateTargetId 수정할 파일id
     */
    void updatePdfById(MultipartFile file, Long updateTargetId);

    /**
     * 수정할 위치에 여러 개의 파일이 있는 경우
     * @param file 수정된 파일
     * @param updateTargetId 수정할 파일id
     */
    void updateAnyFileById(MultipartFile file, Long updateTargetId);



    /**
     * 파일 삭제
     * @param fileRequest 삭제할 파일 위치:  ReferenceType, ReferenceId
     * @return 삭제 여부
     */
    boolean deleteFile(FileRequest fileRequest);

    /**
     * 여러개 파일 삭제
     * @param fileRequest 삭제할 파일 위치:  ReferenceType, ReferenceId
     * @return 삭제 여부
     */
    boolean deleteFiles(FileRequest fileRequest);

    /**
     * 아이디로 파일 삭제
     * @param fileId 삭제할 파일 아이디
     * @return 삭제 여부
     */
    boolean deleteFileById(Long fileId);

}
