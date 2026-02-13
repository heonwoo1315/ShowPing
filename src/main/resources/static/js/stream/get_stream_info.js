(async function() {
    try {
        const response = await axios.get("/api/live/live-info", {
            withCredentials: true
        });

        const data = response.data;

        if (typeof data !== "undefined" && data !== "") {
            streamInfo = true;
            streamNo = data.streamNo;
            // [수정] 요소가 존재할 때만 value 설정 (Optional Chaining 방식)
            const titleEl = document.getElementById("broadcastTitle");
            if (titleEl) titleEl.value = data.streamTitle;

            const descEl = document.getElementById("broadcastDesc");
            if (descEl) descEl.value = data.streamDescription;

            const imgEl = document.querySelector(".product-img");
            if (imgEl) imgEl.src = data.productImg;

            const infoEl = document.querySelector(".product-info");
            if (infoEl) infoEl.id = data.productNo;

            const discountEl = document.getElementById("discountRate");
            if (discountEl) discountEl.value = data.productSale;
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