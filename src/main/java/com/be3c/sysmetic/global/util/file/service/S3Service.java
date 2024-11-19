package com.be3c.sysmetic.global.util.file.service;

import com.be3c.sysmetic.global.util.file.dto.FileRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private Region region;


    /**
     * 파일 저장 경로를 참조id로 폴더화
     * e.g, 참조id가 110000 -> 참조테이블/0/0000/0000/0011/0000/파일명
     */
    private static String createFolderStructure(long id) {
        long part1 = id / 1000000000000L;         // 첫 번째 3자리
        long part2 = (id / 100000000) % 10000;    // 두 번째 4자리
        long part3 = (id / 1000000) % 10000;      // 세 번째 4자리
        long part4 = (id / 10000) % 10000;        // 네 번째 4자리
        long part5 = id % 10000;                  // 다섯 번째 4자리

        return String.format("/%d/%04d/%04d/%04d/%04d/", part1, part2, part3, part4, part5);
    }


    /**
     * S3 버킷에 업로드
     * @param file 업로드 할 파일
     * @param fileRequestDto 폴더 스트럭쳐를 만들기 위한 reference 정보
     * @param extention 검증된 파일 확장자 e.g) .png
     * @return 업로드 한 경로, s3의 keyName
     */
    public String upload(MultipartFile file, FileRequestDto fileRequestDto, String extention) {

        String structuredReferenceId = createFolderStructure(fileRequestDto.referenceId());
        String keyName = fileRequestDto.referenceType() + structuredReferenceId + UUID.randomUUID() + extention;

        try (InputStream inputStream = file.getInputStream()) {
        RequestBody requestBody = RequestBody.fromInputStream(inputStream, file.getSize());

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(keyName)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build(),
                requestBody);

        } catch (IOException e) {
            throw new RuntimeException("파일 입력 오류로 S3 파일 업로드에 실패했습니다: " + e, e);
        }

        return keyName;
    }


    /**
     * Create a pre-signed URL to download an object in a subsequent GET request
     * @param keyName S3에 저장된 key
     * @return presignedGetURL
     */
    public String createPresignedGetUrl(String keyName) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // URL duration
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
//            logger.info("Presigned URL: [{}]", presignedRequest.url().toString());
//            logger.info("HTTP method: [{}]", presignedRequest.httpRequest().method());
        System.out.println("Presigned URL: [{}]"+ presignedRequest.url().toString());
        System.out.println("HTTP method: [{}]" + presignedRequest.httpRequest().method());

        return presignedRequest.url().toExternalForm();
    }

    /**
     * 버킷에 있는 파일 삭제
     * @param keyName S3에 저장된 key
     * @return 삭제 true/false
     */
    public boolean deleteObject(String keyName) {

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        DeleteObjectResponse response = s3Client.deleteObject(deleteObjectRequest);

        return response.sdkHttpResponse().isSuccessful();
    }

    public String updateObject(MultipartFile file, FileRequestDto fileRequestDto, String keyName){

        try (InputStream inputStream = file.getInputStream()) {

            RequestBody requestBody = RequestBody.fromInputStream(inputStream, file.getSize());
            deleteObject(keyName);

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(keyName)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    requestBody);

        } catch (IOException e) {
            throw new RuntimeException("파일 입력 오류로 S3 파일 업로드에 실패했습니다: " + e, e);
        }

        return keyName;
    }

}