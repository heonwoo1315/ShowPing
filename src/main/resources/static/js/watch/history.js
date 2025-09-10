let currentPage = 0;
const pageSize = 5;
let isLast = false;

document.addEventListener("DOMContentLoaded", function () {
    loadWatchHistory();
    document.getElementById("load-more-btn").addEventListener("click", loadWatchHistory);
});

function loadWatchHistory() {
    if (isLast) {
        return;
    }

    axios.get(`/api/watch/history/list/page`, {
        params: {
            pageNo: currentPage,
            pageSize: pageSize,
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