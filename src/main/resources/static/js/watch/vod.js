const videoElement = document.getElementById('vod'); // id 'vod'와 일치
const track = videoElement.addTextTrack("subtitles", "Korean", "ko");
let chatMessages = [];
let nextChatIndex = 0;

window.streamStartTime = new Date(window.streamStartTime);


// 수동 파싱
function parseChatCreatedAt(timestampStr) {
    // 수동 파싱 방식
    const [datePart, timePart] = timestampStr.split(' ');
    if (!datePart || !timePart) return null;
    const [year, month, day] = datePart.split('-').map(Number);
    let [hms, msecStr] = timePart.split('.');
    if (!msecStr) msecStr = '000';
    msecStr = msecStr.slice(0, 3);
    const [hour, minute, second] = hms.split(':').map(Number);
    const parsedDate = new Date(year, month - 1, day, hour, minute, second, parseInt(msecStr, 10));
    return parsedDate;
}

// 채팅 발생 시간과 스트림 시작 시간의 차이를 초 단위로 계산
function getOffsetSeconds(chatTimeStr) {
    const chatDate = parseChatCreatedAt(chatTimeStr);
    if (!chatDate) { // 메시지 건너뛰었을 때
        return NaN;
    }
    const offsetMs = chatDate.getTime() - window.streamStartTime.getTime();
    return offsetMs / 1000;
}

function fetchChatMessages(chatStreamNo) {
    const accessToken = sessionStorage.getItem('accessToken');

    axios.get('/chat/api/messages', {
        params: {chatStreamNo: chatStreamNo},
        headers: {
            "Authorization": "Bearer " + accessToken
        }
    })
        .then(response => {
            chatMessages = response.data; // 전역 변수에 할당

            // 각 메시지에 대해 offsetSeconds 계산
            chatMessages.forEach(msg => {
                msg.offsetSeconds = getOffsetSeconds(msg.chat_created_at);
            });

            // offsetSeconds 기준 오름차순 정렬
            chatMessages.sort((a, b) => a.offsetSeconds - b.offsetSeconds);
            nextChatIndex = 0;
        })
        .catch(error => {
            console.error("채팅 메시지를 불러오는 중 오류 발생:", error);
        });
}

function updateChatMessages() {
    const currentSec = videoElement.currentTime;  // 'vod' 비디오 요소의 currentTime 사용

    // 각 메시지에 대해 반복하면서 현재 재생 시간과 offsetSeconds 비교
    chatMessages.forEach((msg, idx) => {
        let fixedStr = msg.chat_created_at.replace(/(\.\d{3})\d+/, '$1')
            .replace(' ', 'T');
        let parsedDate = new Date(fixedStr);

        // parsedDate가 Invalid Date인지 확인
        if (isNaN(parsedDate.getTime())) {
            console.error(`[ERROR] Date 파싱 실패(${idx}):`, fixedStr);
        }

        msg.offsetSeconds = (parsedDate - window.streamStartTime) / 1000;
    });

    // nextChatIndex부터 모든 메시지 중 아직 출력되지 않은 메시지에 대해 검사
    for (let i = nextChatIndex; i < chatMessages.length; i++) {
        const msg = chatMessages[i];
        // 로그: 각 메시지에 대한 offsetSeconds와 비교 결과
        if (!msg.displayed && msg.offsetSeconds <= currentSec) {
            appendChatMessage(msg);
            msg.displayed = true;
            nextChatIndex = i + 1;  // 출력된 이후 다음 인덱스로 업데이트
        } else {
            // 조건에 맞지 않으면 break: 아직 출력할 메시지가 없으므로 루프 종료
            break;
        }
    }
    requestAnimationFrame(updateChatMessages);
}

// 사용자가 영상의 시크(seek) 이벤트로 재생 시간을 변경할 때 채팅 영역을 새로 갱신하는 함수
function updateChatOnSeek() {
    const currentSec = videoElement.currentTime;
    const chatContainer = document.getElementById('chat-messages');
    // 채팅 영역을 비웁니다.
    chatContainer.innerHTML = "";
    // 모든 메시지의 displayed 플래그를 false로 재설정
    chatMessages.forEach(msg => {
        msg.displayed = false;
    });
    // 현재 재생 시간에 해당하는 채팅 메시지들만 다시 출력
    let newNextIndex = 0;
    chatMessages.forEach((msg, idx) => {
        if (msg.offsetSeconds <= currentSec) {
            appendChatMessage(msg);
            msg.displayed = true;
            newNextIndex = idx + 1;
        }
    });
    nextChatIndex = newNextIndex;
}

// ================= 수정된 appendChatMessage 함수 =================
// 기존 단순 텍스트 출력 방식 대신, stream.js의 addMessageToChat()와 유사한 구조로 수정
function appendChatMessage(msg) {
    const chatContainer = document.getElementById('chat-messages');

    // 새 메시지 요소 생성
    const messageElement = document.createElement("div");
    messageElement.classList.add("message"); // stream.js와 동일한 클래스 사용

    // 사용자 아이디 표시용 span 생성
    const userNameSpan = document.createElement("span");
    userNameSpan.classList.add("user-name");

    // 메시지 텍스트 표시용 p 태그 생성
    const messageTextP = document.createElement("p");
    messageTextP.classList.add("chat-text");

    // ROLE에 따른 처리
    if (msg.chat_role && msg.chat_role === "ROLE_ADMIN") {
        // 관리자라면 표시 텍스트 변경 및 스타일 적용
        userNameSpan.textContent = "관리자 ✓";
        userNameSpan.style.color = "red";
        messageElement.classList.add("admin");
        messageTextP.style.color = "red";
        messageTextP.textContent = msg.chat_message;
    } else {
        // 일반 사용자의 경우
        userNameSpan.textContent = msg.chat_member_id;
        messageTextP.textContent = msg.chat_message;
    }

    // 요소 조합
    messageElement.appendChild(userNameSpan);
    messageElement.appendChild(messageTextP);

    // 채팅 메시지 컨테이너에 추가
    chatContainer.appendChild(messageElement);

    // 자동 스크롤 (추가된 부분)
    setTimeout(() => {
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }, 100);
}

function addWatch(streamNo) {
    const accessToken = sessionStorage.getItem('accessToken');
    const watchTime = new Date();

    axios.post('/watch/insert',
        {
            streamNo: streamNo,
            watchTime: watchTime
        }, {
            headers: {
                "Authorization": "Bearer " + accessToken
            }
        });
}

// 자막을 불러오는 메서드
function fetchSubtitle(title) {
    // 최초에는 자막모드를 비활성화
    track.mode = "disabled";

    // axios 활용 자막정보 가져오기
    axios.get(`/stream/subtitle/${title}.json`)
        .then(response => response.data)
        .then(data => {
            // segment 별로 TextTrack 추가
            data.segments.forEach(segment => {
                let cue = new VTTCue(
                    msToSeconds(segment.start),
                    msToSeconds(segment.end),
                    ""
                );
                cue.words = segment.words;
                cue.fullText = segment.text;
                track.addCue(cue);
            })
        });
}

// 밀리초를 초 단위로 변환하는 함수
function msToSeconds(ms) {
    return ms / 1000;
}

async function streamVideo(title) {
    if (Hls.isSupported()) {
        var hls = new Hls();
        hls.loadSource(`/vod/v2/flux/${title}.m3u8`);
        hls.attachMedia(videoElement);
    } else if (videoElement.canPlayType('application/vnd.apple.mpegurl')) {
        videoElement.src = `/vod/v2/flux/${title}.m3u8`;
    }
}

// 자막모드 활성화
function onSubtitle() {
    if (track.mode === "disabled") {
        track.mode = "showing";
    }
}

// 자막모드 비활성화
function offSubtitle() {
    if (track.mode === "showing") {
        track.mode = "disabled";
    }
}

// 영상의 시간이 바뀔대마다 자막정보 update
videoElement.addEventListener('timeupdate', () => {
    // 자막모드가 비활성화일때는 수행하지 않음
    if (track.mode !== 'showing') {
        return;
    }

    // 현재 활성화된 자막 가져오기
    if (track.activeCues && track.activeCues.length > 0) {
        const activeCue = track.activeCues[0]; // 활성 cue 하나를 대상으로 처리
        let currentTime = videoElement.currentTime;
        let displayedText = "";

        // 어절별로 자막을 붙여서 자막 표현
        activeCue.words.forEach(word => {
            // word.start가 밀리초 단위이므로 비교 전 변환
            if ((currentTime * 1000) >= word.start) {
                displayedText += (displayedText ? " " : "") + word.text;
            }
        });
        activeCue.text = displayedText;
    }
});

function controlTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    const faqItems = document.querySelectorAll('.faq-item');

    faqItems.forEach(item => {
        const question = item.querySelector('.faq-question');

        // 질문 영역 클릭 시 active 토글
        question.addEventListener('click', () => {
            // 이미 active라면 닫고, 아니면 열기
            item.classList.toggle('active');
        });
    });

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            // 1) 모든 버튼에서 active 제거
            tabButtons.forEach(b => b.classList.remove('active'));
            // 2) 모든 탭 콘텐츠에서 active 제거
            tabContents.forEach(tc => tc.classList.remove('active'));

            // 3) 클릭된 버튼에 active 추가
            btn.classList.add('active');
            // 4) data-target 속성으로 연결된 콘텐츠를 찾아서 active 추가
            const targetId = btn.getAttribute('data-target');
            const targetContent = document.getElementById(targetId);
            if (targetContent) {
                targetContent.classList.add('active');
            }
        });
    });
}

videoElement.addEventListener('seeked', () => {
    // 시크(seek) 이벤트 발생 시 채팅 영역 업데이트
    updateChatOnSeek();
});