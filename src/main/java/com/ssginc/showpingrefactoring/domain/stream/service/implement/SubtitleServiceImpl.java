package com.ssginc.showpingrefactoring.domain.stream.service.implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ssginc.showpingrefactoring.infrastructure.NCP.storage.StorageLoader;
import com.ssginc.showpingrefactoring.infrastructure.NCP.subtitle.Segments;
import com.ssginc.showpingrefactoring.infrastructure.NCP.subtitle.SubtitleGenerator;
import com.ssginc.showpingrefactoring.domain.stream.service.SubtitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author dckat
 * 자막과 관련한 로직을 구현한 서비스 클래스
 * <p>
 */
@Service
@RequiredArgsConstructor
public class SubtitleServiceImpl implements SubtitleService {

    private final SubtitleGenerator subtitleGenerator;

    private final StorageLoader storageLoader;

    /**
     * 영상 제목으로 자막 파일을 생성하고 저장하는 메서드
     * @param title 영상 제목
     */
    @Override
    public void createSubtitle(String title) {
        // 자막 정보 불러오기
        List<Segments> segments = subtitleGenerator.getSubtitles(title);

        // 저장할 파일이름 지정
        String fileName = title + ".json";
        File jsonFile = new File(fileName);

        ObjectMapper mapper = new ObjectMapper();

        // 최상위 노드 생성
        ObjectNode root = mapper.createObjectNode();
        root.set("segments", mapper.valueToTree(segments));

        // json 파일 생성
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, root);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 생성된 json 파일을 NCP Storage 업로드
        storageLoader.uploadSubtitleFile(jsonFile);
        jsonFile.delete();
    }

    /**
     * 지정된 파일 이름으로 json 자막파일 가져오는 메서드
     * @param title    파일 이름
     * @return 자막 json 파일
     */
    @Override
    public Resource getSubtitle(String title) {
        String fileName = title + ".json";
        return storageLoader.getSubtitle(fileName);
    }

}
