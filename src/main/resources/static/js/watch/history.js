let currentPage = 0;
const pageSize = 5;
let isLast = false;

let fromDate;
let toDate = new Date().toISOString();

document.addEventListener("DOMContentLoaded", function () {
    loadWatchHistory();
    setFilterButtons();
    setLoadMoreButton();
});

function setFilterButtons() {
    const chips = document.querySelectorAll('.filter-chip-group .chip');
    const fromInput = document.getElementById('from');
    const toInput = document.getElementById('to');

    const formatDate = (d) => d.toISOString().slice(0, 10);

    // range 값을 기준으로 시작일/종료일 계산
    const setPeriod = (range) => {
        const today = new Date();
        const end = new Date(today);
        const start = new Date(today);

        if (range === '7d') {
            start.setDate(end.getDate() - 6);
        } else if (range === '1m') {
            start.setMonth(end.getMonth() - 1);
        } else if (range === '3m') {
            start.setMonth(end.getMonth() - 3);
        } else if (range === '6m') {
            start.setMonth(end.getMonth() - 6);
        } else {
            return; // 직접 입력
        }

        fromInput.value = formatDate(start);
        toInput.value = formatDate(end);
    };

    chips.forEach(chip => {
        chip.addEventListener('click', () => {
            const range = chip.dataset.range;

            // active 표시 갱신
            chips.forEach(c => c.classList.remove('is-active'));
            chip.classList.add('is-active');

            // 날짜 input 값 갱신
            setPeriod(range);
        });
    });
}

function setLoadMoreButton() {
    document.getElementById("load-more-btn").addEventListener("click", loadWatchHistory);
}

function loadWatchHistory() {
    if (isLast) {
        return;
    }

    axios.get(`/api/watch/v1/history/list/page`, {
        params: {
            pageNo: currentPage,
            pageSize: pageSize,
            fromDate: fromDate,
            toDate: toDate,
            sort: "recent",
        },
        withCredentials: true // 쿠키 인증 방식
    })
        .then(response => {
            const {content, pageInfo} = response.data;
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

            currentPage++;
            isLast = pageInfo.last;

            if (isLast) {
                document.getElementById("load-more-btn").style.display = "none";
            }
        })
        .catch(error => {
            console.error("시청 이력 데이터를 불러오는 중 오류 발생:", error);
        });
}