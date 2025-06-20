let memberNo = null;
let orders = []; // 주문 목록을 전역 변수에 저장

document.addEventListener("DOMContentLoaded", async function () {

    try {
        // 로그인 사용자 정보 조회
        const response = await axios.get("/api/carts/info", {
            withCredentials: true
        });
        memberNo = response.data.memberNo;
        if (!memberNo) {
            alert("사용자 정보가 없습니다.");
            return;
        }

        // 회원 전체 주문 목록 가져오기
        const orderResponse = await axios.get(`/api/orders/member/${memberNo}`);
        orders = orderResponse.data;  // 전역 변수에 저장

        // 주문 상세 정보를 미리 가져와 각 주문에 저장 (추후 검색 시 추가 API 호출 없이 사용)
        await Promise.all(orders.map(async (order) => {
            try {
                const detailResponse = await axios.get(`/api/orders/${order.ordersNo}/details`);
                order.orderDetails = detailResponse.data;
            } catch (error) {
                console.error(`주문 ${order.ordersNo} 상세 정보 조회 실패:`, error);
                order.orderDetails = []; // 실패 시 빈 배열로 처리
            }
        }));

        // 초기 로딩 시 전체 주문 목록 렌더링
        renderOrders(orders);

        // 단일 검색 버튼 이벤트 (날짜, 상품명 동시에 검색)
        const searchButton = document.getElementById("search-button");
        searchButton.addEventListener("click", function () {
            const startDateVal = document.getElementById("start-date").value; // "YYYY-MM-DD" 형식
            const endDateVal = document.getElementById("end-date").value;   // "YYYY-MM-DD" 형식
            const productSearchVal = document.getElementById("product-name").value.trim();

            const filtered = orders.filter(order => {
                // 날짜 필터링: 주문 일시가 입력한 날짜 범위 내에 있는지 확인
                const orderDate = new Date(order.ordersDate);

                if (startDateVal && orderDate < new Date(startDateVal)) return false;

                if (endDateVal) {
                    const endDate = new Date(endDateVal);
                    endDate.setHours(23, 59, 59, 999);
                    if (orderDate > endDate) return false;
                }

                // 상품명 검색: 입력한 문자열이 주문 상세 정보에 포함되어 있는지 (대소문자 무시)
                if (productSearchVal) {
                    if (!order.orderDetails ||
                        !order.orderDetails.some(item => item.productName.toLowerCase().includes(productSearchVal.toLowerCase()))) {
                        return false;
                    }
                }
                return true;
            });

            renderOrders(filtered);
        });

        // 초기화 버튼: 입력 필드 모두 초기화하고 전체 주문 목록 표시
        const resetButton = document.getElementById("reset-button");
        resetButton.addEventListener("click", function () {
            document.getElementById("start-date").value = "";
            document.getElementById("end-date").value = "";
            document.getElementById("product-name").value = "";
            renderOrders(orders);
        });

    } catch (error) {
        console.error("주문 정보 불러오기 실패:", error);
        window.location.href = "/login";
    }
});

function renderOrders(orderList) {
    const orderContainer = document.getElementById("order-list");
    orderContainer.innerHTML = ""; // 기존 내용 초기화

    if (!orderList || orderList.length === 0) {
        orderContainer.innerHTML = "<p class='no-orders'>최근 주문 내역이 없습니다.</p>";
        return;
    }

    orderList.forEach(order => {
        let orderStatusText = "";
        switch (order.ordersStatus) {
            case "READY":
                orderStatusText = "상품 준비 중";
                break;
            case "TRANSIT":
                orderStatusText = "배송 중";
                break;
            case "COMPLETE":
                orderStatusText = "배송 완료";
                break;
            default:
                orderStatusText = order.ordersStatus;
        }

        const orderDiv = document.createElement("div");
        orderDiv.classList.add("order-box");
        orderDiv.innerHTML = `
            <div class="order-header">
                <h3>주문 번호: ${order.ordersNo}</h3>
                <span class="status ${order.ordersStatus.toLowerCase()}">${orderStatusText}</span>
            </div>
            <p><strong>총 결제 금액:</strong> ${order.ordersTotalPrice.toLocaleString()} 원</p>
            <p><strong>주문 일시:</strong> ${new Date(order.ordersDate).toLocaleString()}</p>
            <button class="toggle-details" data-order-no="${order.ordersNo}">상세 보기</button>
            <div class="order-details" id="order-details-${order.ordersNo}" style="display: none;">
                <ul id="order-items-${order.ordersNo}"></ul>
            </div>
        `;
        orderContainer.appendChild(orderDiv);
    });

// 상세보기 버튼 이벤트 (기존 코드 재활용)
    document.querySelectorAll(".toggle-details").forEach(button => {
        button.addEventListener("click", async function () {
            const orderNo = this.getAttribute("data-order-no");
            const detailsDiv = document.getElementById(`order-details-${orderNo}`);

            // 이미 열려 있다면 닫기
            if (detailsDiv.style.display === "block") {
                detailsDiv.style.display = "none";
                this.textContent = "상세 보기";
                return;
            }

            // 숨겨져 있으면 상세 내역 요청
            try {
                const orderDetailResponse = await axios.get(`/api/orders/${orderNo}/details`);
                const orderDetails = orderDetailResponse.data;

                const orderItemsList = document.getElementById(`order-items-${orderNo}`);
                orderItemsList.innerHTML = ""; // 기존 리스트 초기화

                orderDetails.forEach(item => {
                    const li = document.createElement("li");
                    li.innerHTML = `상품 이름: ${item.productName} | 수량: ${item.orderDetailQuantity} | 가격: <strong>${item.orderDetailTotalPrice.toLocaleString()} 원</strong>`;
                    orderItemsList.appendChild(li);
                });

                detailsDiv.style.display = "block";
                this.textContent = "상세 보기 닫기";
            } catch (error) {
                console.error(`주문 ${orderNo}의 상세 정보를 불러오는 중 오류 발생:`, error);
            }
        });
    });
}

// (예시) 연도별 검색 버튼 이벤트 처리
document.querySelectorAll(".year-btn").forEach(button => {
    button.addEventListener("click", function () {
        const year = parseInt(this.getAttribute("data-year"));
        const filtered = orders.filter(order => new Date(order.ordersDate).getFullYear() === year);
        renderOrders(filtered);
    });
});