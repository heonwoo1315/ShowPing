package com.ssginc.showpingrefactoring.common.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dckat
 * NCP Storage 설정 클래스
 * <p>
 */
@Configuration
public class NCPConfig {

    // NCP region 정보
    @Value("${ncp.storage.region}")
    private String region;

    // 엔드포인트 주소
    @Value("${ncp.storage.endpoint}")
    private String endPoint;

    // access key
    @Value("${ncp.storage.access-key}")
    private String accessKey;

    // secret key
    @Value("${ncp.storage.secret-key}")
    private String secretKey;

    /**
     * 설정된 정보로 S3 bean 생성하는 메서드
     * @return S3 Client 객체
     */
    @Bean
    public AmazonS3Client objectStorageClient() {
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }

}