// payments.js  — CSRF 유틸(ensureCsrfCookie / csrfPost) 적용 버전

let memberNo = null;

document.addEventListener("DOMContentLoaded", async function () {
    try {
        // 로그인 사용자/장바구니 정보
        const response = await axios.get("/api/carts/info", { withCredentials: true });
        console.log("로그인된 사용자 정보:", response.data);

        memberNo = response.data.memberNo;
        if (!memberNo) {
            alert("사용자 정보가 없습니다.");
            return;
        }

        if (response.data) {
            document.getElementById("name").value = response.data.memberName || "";
            document.getElementById("phone").value = response.data.memberPhone || "";
            document.getElementById("email").value = response.data.memberEmail || "";
            document.getElementById("address").value = response.data.memberAddress || "";
        }

        // 장바구니 선택 항목 렌더링
        const selectedItems = JSON.parse(sessionStorage.getItem("selectedItems")) || [];
        const orderItemsContainer = document.getElementById("order-items");
        let totalPrice = 0;

        if (selectedItems.length === 0) {
            orderItemsContainer.innerHTML = "<p>선택된 상품이 없습니다.</p>";
            return;
        }

        selectedItems.forEach(item => {
            const itemElement = document.createElement("div");
            itemElement.classList.add("order-item");
            itemElement.innerHTML = `
        <a class="item-name">${item.name} x ${item.quantity}</a> 
        <a class="item-price">${item.totalPrice.toLocaleString()} 원</a>
      `;
            orderItemsContainer.appendChild(itemElement);
            totalPrice += item.totalPrice;
        });

        document.getElementById("total-price").textContent = `${totalPrice.toLocaleString()} 원`;

    } catch (error) {
        console.error("사용자 정보를 불러오는 중 오류 발생:", error);
        window.location.href = "/login";
    }
});

// 결제 완료 버튼 클릭 시 주문 저장
document.addEventListener("DOMContentLoaded", function () {
    const paymentButton = document.querySelector(".payment-button");

    paymentButton.addEventListener("click", async function () {
        const name = document.getElementById("name").value.trim();
        const phone = document.getElementById("phone").value.trim();
        const email = document.getElementById("email").value.trim();
        const address = document.getElementById("address").value.trim();
        const totalPrice = parseInt(
            document.getElementById("total-price").textContent.replace(" 원", "").replaceAll(",", ""),
            10
        );
        const selectedItems = JSON.parse(sessionStorage.getItem("selectedItems")) || [];

        if (!name || !phone || !email || totalPrice <= 0) {
            alert("모든 정보를 입력하고, 결제 금액을 확인해주세요.");
            return;
        }
        if (selectedItems.length === 0) {
            alert("결제할 상품이 없습니다.");
            return;
        }
        if (!window.PortOne) {
            alert("결제 시스템을 불러오는 중입니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        function generatePaymentId() {
            return [...crypto.getRandomValues(new Uint32Array(2))]
                .map((word) => word.toString(16).padStart(8, "0"))
                .join("");
        }

        const paymentId = generatePaymentId();
        console.log(paymentId);

        try {
            // 결제 요청
            const payment = await PortOne.requestPayment({
                storeId: storeId,
                channelKey: channelKey,
                paymentId,
                orderName: "상품 결제",
                totalAmount: totalPrice,
                currency: "KRW",
                payMethod: "CARD",
                customer: { name, email, phone },
                customData: { userInfo: { name, phone, email } },
            });

            if (payment.code !== undefined) {
                Swal.fire({
                    title: "결제 실패",
                    text: payment.message,
                    icon: "error",
                    confirmButtonText: "확인"
                });
                return;
            }

            // CSRF 유틸 적용: 쿠키 보장 후, 주문 생성 API 호출
            await window.ensureCsrfCookie();  // 없으면 /api/csrf 호출로 XSRF-TOKEN 발급
            const orderRes = await window.csrfPost("/api/orders/create", {
                memberNo,
                totalPrice,
                orderItems: selectedItems.map(item => ({
                    productNo: item.productNo,
                    quantity: item.quantity,
                    totalPrice: item.totalPrice
                }))
            });

            if (orderRes && orderRes.status >= 200 && orderRes.status < 300) {
                Swal.fire({
                    title: '결제 성공',
                    text: '결제가 성공적으로 완료되었습니다!',
                    icon: 'success',
                    showCancelButton: true,
                    confirmButtonText: '주문 내역 이동',
                    cancelButtonText: '홈페이지 이동'
                }).then((result) => {
                    if (result.isConfirmed) {
                        window.location.href = "/success";
                    } else {
                        window.location.href = "/";
                    }
                });
            } else {
                Swal.fire({
                    title: "결제 중 오류",
                    text: "결제 중 오류가 발생했습니다. 다시 시도해주세요.",
                    icon: "error",
                    confirmButtonText: "확인"
                });
            }

        } catch (error) {
            console.error("결제 중 오류 발생:", error);
            Swal.fire({
                title: "결제 중 오류",
                text: "결제 중 오류가 발생했습니다. 다시 시도해주세요.",
                icon: "error",
                confirmButtonText: "확인"
            });
        }
    });
});
