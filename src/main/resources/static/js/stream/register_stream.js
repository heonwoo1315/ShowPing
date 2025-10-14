document.addEventListener("DOMContentLoaded", function () {
    const updateBtn = document.querySelector(".update-btn"); // 업데이트 버튼
    if (!updateBtn) return;

    updateBtn.addEventListener("click", async function () {
        // streamNo는 전역으로 선언된 streamNo 사용

        // 입력값 수집
        const streamTitle = document.getElementById("broadcastTitle").value;
        const streamDescription = document.getElementById("broadcastDesc").value;
        const productDiv = document.querySelector(".product-element .product-info");
        const productNo = productDiv ? productDiv.id : null;
        const productSale = parseInt(document.getElementById("discountRate").value, 10);

        // 간단 검증
        if (!streamTitle || !productNo) {
            Swal.fire({ icon: 'info', title: "입력 오류", text: "방송 제목 입력과 상품 선택은 필수입니다." });
            return;
        }
        if (productSale > 100 || productSale < 0 || Number.isNaN(productSale)) {
            Swal.fire({ icon: 'info', title: "입력 오류", text: "할인율은 0~100%로 입력해주세요." });
            return;
        }

        const data = {
            streamNo: streamNo,
            streamTitle: streamTitle,
            streamDescription: streamDescription,
            productNo: productNo,
            productSale: productSale
        };

        try {
            // 1) XSRF 쿠키 보장
            await window.ensureCsrfCookie();
            // 2) 403 시 자동 재시도까지 하는 래퍼로 호출
            const response = await window.csrfPost("/api/live/register", data);

            Swal.fire({
                icon: 'success',
                title: "방송 정보 업데이트 완료",
                text: "업데이트가 완료되었습니다."
            });

            // 서버가 최신 streamNo를 내려주는 경우 반영
            streamNo = response.data.streamNo;

            $('#start').attr('disabled', false);
            $('#stop').attr('disabled', false);
        } catch (error) {
            console.error("방송 정보 업데이트 실패:", error);
            Swal.fire({
                icon: 'error',
                title: '업데이트 실패',
                text: '방송 정보 업데이트 중 문제가 발생했습니다.'
            });
        }
    });
});