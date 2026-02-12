const pageSize = 5;
let isLast = false;
let reset = false;      // 필터링으로 인한 조회내역 리셋여부

let activeRange = 'all';
let fromDate;
let toDate = toLocalTimeString(new Date());

let cursorTime;
let cursorNo;

document.addEventListener("DOMContentLoaded", async function () {
    setFilterForm();
    setFilterButtons();
    setLoadMoreButton();

    await loadWatchHistory();
});

function toLocalTimeString(date = new Date()) {
    const pad = (n) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T` +
        `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function setFilterButtons() {
    const chips = document.querySelectorAll('.filter-chip-group .chip');
    const fromInput = document.getElementById('from');
    const toInput = document.getElementById('to');

    const formatDate = (d) => {
        const offset = d.getTimezoneOffset() * 60000;
        const localISOTime = (new Date(d - offset)).toISOString().slice(0, 10);
        return localISOTime; // 로컬 날짜 문자열 반환
    };

    // range 값을 기준으로 시작일/종료일 계산
    const setPeriod = (range) => {
        // 전체 조회
        if (range === 'all') {
            fromDate = null;
            fromInput.value = '';
            toInput.value = '';
            return true;
        }

        const today = new Date();
        toDate = new Date(today);
        fromDate = new Date(today);

        if (range === '7d') {
            fromDate.setDate(toDate.getDate() - 7);
        } else if (range === '1m') {
            fromDate.setMonth(toDate.getMonth() - 1);
        } else if (range === '3m') {
            fromDate.setMonth(toDate.getMonth() - 3);
        } else if (range === '6m') {
            fromDate.setMonth(toDate.getMonth() - 6);
        } else {
            return true; // 직접 입력
        }

        fromInput.value = formatDate(fromDate);
        toInput.value = formatDate(toDate);

        return true;
    };

    chips.forEach(chip => {
        chip.addEventListener('click', async () => {
            const range = chip.dataset.range;

            // active 표시 갱신
            chips.forEach(c => c.classList.remove('is-active'));
            chip.classList.add('is-active');

            // 날짜 input 값 갱신
            const updated = setPeriod(range);
            activeRange = range;

            reset = true;
            currentPage = 0;

            if (updated) {
                await loadWatchHistory();
            }
        });
    });
}

function validateDates() {
    const fromInput = document.getElementById('from');
    const toInput   = document.getElementById('to');

    const fromVal = (fromInput?.value || '').trim();
    const toVal   = (toInput?.value   || '').trim();

    // 값 누락 체크 (전체 모드라면 from 누락 허용)
    const needFrom = (window.activeRange ? window.activeRange !== 'all' : true);
    if ((needFrom && !fromVal) || !toVal) {
        Swal.fire({
            icon: 'error',
            title: '조회 불가',
            text: '시작일 또는 종료일을 입력받아야 합니다.'
        });
        (!fromVal && needFrom ? fromInput : toInput)?.focus();
        return false;
    }

    // 순서 체크: 시작일은 종료일과 같거나 이전이어야 함.
    if (fromVal && toVal && new Date(fromVal) > new Date(toVal)) {
        Swal.fire({
            icon: 'error',
            title: '조회 불가',
            text: '시작일은 종료일보다 같거나 이전이어야 합니다.'
        });
        fromInput?.focus();
        return false;
    }
    return true;
}

// input 상태 동기화
function syncStateFromInputs() {
    const fromInput = document.getElementById('from');
    const toInput = document.getElementById('to');

    fromDate = fromInput.value ? new Date(fromInput.value) : null;
    toDate = toInput.value ? new Date(toInput.value) : new Date();

    return true;
}

function setFilterForm() {
    const form = document.getElementById('history-filter-form');
    const rangeHidden = document.getElementById('range');

    // 조회 버튼 클릭시
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!validateDates()) {
            return;
        }

        syncStateFromInputs();
        activeRange = (rangeHidden?.value) || 'custom';

        // 조회 리셋
        reset = true;
        await loadWatchHistory();
    });
}

function setLoadMoreButton() {
    document.getElementById("load-more-btn").addEventListener("click", loadWatchHistory);
}

// 테이블 비우기
function clearHistoryTable() {
    const tableBody = document.querySelector(".history-items tbody");
    if (tableBody) tableBody.innerHTML = "";

    // 상태 복구
    isLast = false;
    cursorTime = null;
    cursorNo = null;

    // 더보기 버튼 모드 초기화
    const btn = document.getElementById("load-more-btn");
    if (btn) btn.style.display = "";

    reset = false;
}

async function loadWatchHistory() {
    // 조회 내역 필터링 기준 변경
    if (reset) {
        clearHistoryTable();
    }

    if (isLast) {
        return;
    }

    // 파라미터 정제
    const params = {
        pageSize: pageSize,
        cursorTime: cursorTime,
        cursorStreamNo: cursorNo,
        // 종료일은 해당 날짜의 가장 마지막 시각(23:59:59)으로 설정하는 것이 안전
        toDate: toDate ? toLocalTimeString(new Date(new Date(toDate).setHours(23,59,59))) : toLocalTimeString(new Date())
    };

    // 시작일이 있을 때만 추가 (전체 조회 대응)
    if (fromDate) {
        params.fromDate = toLocalTimeString(new Date(new Date(fromDate).setHours(0,0,0)));
    }

    axios.get(`/api/watch/history/list/scroll`, {
        params, withCredentials: true // 쿠키 인증 방식
    })
        .then(response => {
            const {content, hasMore, nextCursor} = response.data;
            const tableBody = document.querySelector(".history-items tbody");

            content.forEach(item => {
                const date = new Date(item.watchTime);
                const watchDate = date.toLocaleDateString();
                const row = `
                    <tr>
                        <td class="product-order">
                            <img class="product-img" src="${item.productImg}" alt="${item.productName}">
                        </td>
                        <td class="stream-title" data-stream-title="${item.streamTitle}">
                            ${item.streamTitle}
                        </td>
                        <td class="product-name" data-product-name="${item.productName}">
                            ${item.productName}
                        </td>
                        <td class="product-price" data-product-price="${item.productPrice}">
                            ${(item.productPrice).toLocaleString('ko-KR')}원
                        </td>
                        <td class="watch-time" data-stream-time="${item.watchTime}">
                            ${watchDate}
                        </td>
                        <td>
                            <button class="watch-button" data-stream-no="${item.streamNo}">시청</button>
                        </td>
                    </tr>
                `;

                tableBody.insertAdjacentHTML("beforeend", row);
            });

            // 동적으로 추가된 시청 버튼에 이벤트 리스너 추가
            document.querySelectorAll(".watch-button").forEach(button => {
                button.addEventListener("click", function() {
                    const streamNo = this.getAttribute("data-stream-no");
                    if (streamNo) {
                        window.location.href = `/watch/vod/${streamNo}`;
                    }
                });
            });

            isLast = !hasMore;

            if (isLast) {
                document.getElementById("load-more-btn").style.display = "none";
            }
            else {
                cursorTime = nextCursor.watchTime;
                cursorNo = nextCursor.streamNo;
            }
        })
        .catch(error => {
            console.error("시청 이력 데이터를 불러오는 중 오류 발생:", error);
        });
}