(async function() {
    try {
        const accessToken = sessionStorage.getItem('accessToken');

        const header = {
            "Authorization": "Bearer " + accessToken,
        }

        const response = await axios.get("/api/stream/stream", {
            headers: header
        });

        const data = response.data;

        if (typeof data !== "undefined" && data !== "") {
            streamInfo = true;
            streamNo = data.streamNo;
            // 기등록된 방송 제목
            document.getElementById("broadcastTitle").value = data.streamTitle;
            // 기등록된 방송 설명
            document.getElementById("broadcastDesc").value = data.streamDescription;

            // 기등록된 방송 상품
            document.querySelector(".product-img").src = data.productImg;
            document.querySelector(".product-info").id = data.productNo;
            document.querySelector(".product-name").textContent = data.productName;
            document.querySelector(".product-origin-price").textContent = data.productPrice;

            // 기등록된 상품 할인율
            document.getElementById("discountRate").value = data.productSale;
        } else {
            document.getElementById("broadcastTitle").value = "";
            document.getElementById("broadcastDesc").value = "";
            document.querySelector(".product-img").src = "";
            document.getElementById("discountRate").textContent = 0;
        }

        const event = new CustomEvent('dataLoaded');
        window.dispatchEvent(event);
    } catch (error) {
        console.error("데이터 로딩 오류", error);
    }
})();