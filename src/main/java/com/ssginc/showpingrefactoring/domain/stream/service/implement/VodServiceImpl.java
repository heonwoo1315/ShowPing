package com.ssginc.showpingrefactoring.domain.stream.service.implement;

import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.infrastructure.storage.StorageLoader;
import com.ssginc.showpingrefactoring.domain.stream.repository.VodRepository;
import com.ssginc.showpingrefactoring.domain.stream.service.VodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VodServiceImpl implements VodService {

    @Value("${download.path}")
    private String VIDEO_PATH;

    private final StorageLoader storageLoader;

    private final VodRepository vodRepository;

    @Override
    public String uploadVideo(String title) {
        String filePath = VIDEO_PATH + title;
        File file = new File(filePath);
        String fileName = file.getName();
        return storageLoader.uploadMp4File(file, fileName);
    }

    /**
     * 전체 Vod 목록을 반환해주는 메서드
     * @return vod 목록
     */
    @Override
    public List<StreamResponseDto> getAllVod() {
        return vodRepository.findAllVod();
    }

    /**
     * 페이징 정보가 포함된 Vod 목록을 반환해주는 메서드
     * @param pageable 페이징 정보 객체
     * @return 페이징 정보가 있는 vod 목록
     */
    @Override
    public Page<StreamResponseDto> getAllVodByPage(Pageable pageable) {
        return vodRepository.findAllVodByPage(pageable);
    }

    /**
     * 특정 카테고리의 vod 목록을 반환하는 메서드
     * @param categoryNo 카테고리 번호
     * @return vod 목록
     */
    @Override
    public List<StreamResponseDto> getAllVodByCategory(Long categoryNo) {
        return vodRepository.findAllVodByCategory(categoryNo);
    }

    @Override
    public Page<StreamResponseDto> getAllVodByWatch(Pageable pageable) {
        return vodRepository.findAllVodByWatch(pageable);
    }

    @Override
    public Page<StreamResponseDto> getAllVodByCategoryAndPage(Long categoryNo, Pageable pageable) {
        return vodRepository.findAllVodByCategoryAndPage(categoryNo, pageable);
    }

    @Override
    public Page<StreamResponseDto> getAllVodByCatgoryAndWatch(Long categoryNo, Pageable pageable) {
        return vodRepository.findAllVodByCategoryAndWatch(categoryNo, pageable);
    }

    /**
     * 영상번호로 VOD 정보를 가져오는 메서드
     * @param streamNo 영상 번호
     * @return 쿼리를 통해 가져온 영상정보 DTO
     */
    @Override
    public StreamResponseDto getVodByNo(Long streamNo) {
        return vodRepository.findVodByNo(streamNo);
    }
}
