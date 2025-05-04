package com.ssginc.showpingrefactoring.chat.service;

import java.util.List;

public interface ForbiddenWordFilterService {
    /**
     * 텍스트에 금칙어가 독립적인 단어로 포함되어 있는지 확인.
     *
     * @param text 검사할 텍스트
     * @return 포함되어 있으면 true, 아니면 false
     */
    boolean containsForbiddenWord(String text);

    /**
     * 텍스트 내에 포함된 독립적인 금칙어 목록 반환.
     *
     * @param text 검사할 텍스트
     * @return 독립적으로 매칭된 금칙어 리스트
     */
    List<String> getForbiddenWords(String text);
}
