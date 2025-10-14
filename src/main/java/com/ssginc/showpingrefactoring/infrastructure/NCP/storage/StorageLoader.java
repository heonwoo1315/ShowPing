package com.ssginc.showpingrefactoring.infrastructure.NCP.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author dckat
 * NCP Storage로 파일 업로드와 다운로드를 담당하는 클래스
 * <p>
 */
@Component
@RequiredArgsConstructor
public class StorageLoader {

    @Value("${ncp.storage.bucket-name}")
    private String bucketName;

    private final AmazonS3 amazonS3Client;

    /**
     * mp4 파일을 NCP에 저장하는 메서드
     * @param file     저장할 파일 mp4
     * @param fileName 영상 제목
     * @return 업로드된 파일 링크
     */
    public String uploadMp4File(File file, String fileName) {
        String remoteKey = "video/" + file.getName();
        amazonS3Client.putObject(new PutObjectRequest(bucketName, remoteKey, file));
        return "video/" + fileName + ".mp4";
    }

    /**
     * 생성된 HLS 파일과 TS를 NCP Storage에 저장하는 메서드
     * @param files    저장할 파일 리스트
     * @return 업로드된 파일 링크
     */
    public String uploadHlsFiles(File[] files, String fileName) {
        if (files != null) {
            for (File file : files) {
                String remoteKey = "video/hls/" + file.getName();
                amazonS3Client.putObject(new PutObjectRequest(bucketName, remoteKey, file));
            }
        }
        else {
            return null;
        }
        return "video/hls/" + fileName + ".m3u8";
    }

    /**
     * 지정된 파일 이름으로 NCP Storage json 자막파일 불러오는 메서드
     * @param fileName 영상 제목
     * @return 자막 json 파일
     */
    public Resource getSubtitle(String fileName) {
        String remoteKey = "text/" + fileName;
        S3Object s3Object = amazonS3Client.getObject(new GetObjectRequest(bucketName, remoteKey));
        return new InputStreamResource(s3Object.getObjectContent());
    }

    /**
     * 지정된 파일 이름으로 NCP Storage에서 HLS 파일들을 불러오는 메서드
     * @param fileName 영상 제목
     * @return 불러온 리소스
     */
    public Resource getHLS(String fileName) {
        String remoteKey = "video/hls/" + fileName;
        S3Object s3Object = amazonS3Client.getObject(new GetObjectRequest(bucketName, remoteKey));
        return new InputStreamResource(s3Object.getObjectContent());
    }

    /**
     * 자막 파일을 NCP에 저장하는 메서드
     * @param file     저장할 자막 파일
     */
    public void uploadSubtitleFile(File file) {
        String remoteKey = "text/" + file.getName();
        amazonS3Client.putObject(new PutObjectRequest(bucketName, remoteKey, file));
    }
}
