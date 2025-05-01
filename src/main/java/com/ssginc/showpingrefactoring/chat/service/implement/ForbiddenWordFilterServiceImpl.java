package com.ssginc.showpingrefactoring.chat.service.implement;

import com.ssginc.showpingrefactoring.chat.dto.object.ForbiddenWord;
import com.ssginc.showpingrefactoring.chat.repository.ForbiddenWordRepository;
import com.ssginc.showpingrefactoring.chat.service.ForbiddenWordFilterService;
import lombok.RequiredArgsConstructor;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author juil1-kim
 * 금칙어 필터링 서비스의 구현체 클래스
 * <p>
 * MongoDB에서 금칙어 목록을 불러와 Aho-Corasick Trie를 구성하고,
 * 텍스트 내 독립적인 금칙어 여부 및 목록 반환.
 */
@Service
@RequiredArgsConstructor
public class ForbiddenWordFilterServiceImpl implements ForbiddenWordFilterService {

    private final ForbiddenWordRepository forbiddenWordRepository;
    private Trie trie;

    /**
     * 프로그램 시작 시 금칙어 목록을 MongoDB에서 불러와 Trie 구성.
     */
    @PostConstruct
    public void init() {
        rebuildTrie();
    }

    /**
     * 주기적으로(5분마다) 금칙어 목록을 갱신하여 Trie 재구성.
     * <p>
     * 24시간마다 금칙어 목록을 불러와 현재 Trie 업데이트.
     */
    @Scheduled(fixedRate = 86400000)  // 24시간마다 실행
    public void rebuildTrie() {
        List<String> forbiddenWords = forbiddenWordRepository.findAll().stream()
                .map(ForbiddenWord::getSlang)
                .collect(Collectors.toList());
        this.trie = Trie.builder()
                .ignoreCase()  // 대소문자 구분 없이 매칭
                .addKeywords(forbiddenWords)
                .build();
    }

    /**
     * 텍스트에 금칙어가 독립적인 단어로 포함되어 있는지 확인.
     *
     * @param text 검사할 텍스트
     * @return 독립된 금칙어가 발견되면 true, 아니면 false
     */
    @Override
    public boolean containsForbiddenWord(String text) {
        Collection<Emit> emits = trie.parseText(text);
        for (Emit emit : emits) {
            if (isIsolatedMatch(text, emit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 텍스트 내에 포함된 독립적인 금칙어 목록 반환.
     *
     * @param text 검사할 텍스트
     * @return 독립적으로 매칭된 금칙어 리스트
     */
    @Override
    public List<String> getForbiddenWords(String text) {
        Collection<Emit> emits = trie.parseText(text);
        return emits.stream()
                .filter(emit -> isIsolatedMatch(text, emit))
                .map(Emit::getKeyword)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 매치된 금칙어가 텍스트 내에서 독립적인 단어인지 확인.
     *
     * @param text  입력 텍스트
     * @param emit  매치 정보(시작, 끝 인덱스 포함)
     * @return 독립된 단어이면 true, 아니면 false
     */
    private boolean isIsolatedMatch(String text, Emit emit) {
        int start = emit.getStart();
        int end = emit.getEnd(); // end는 매치된 마지막 인덱스 (inclusive)
        boolean leftBoundary = (start == 0) || !Character.isLetterOrDigit(text.charAt(start - 1));
        boolean rightBoundary = (end == text.length() - 1) || !Character.isLetterOrDigit(text.charAt(end + 1));
        return leftBoundary && rightBoundary;
    }
}
