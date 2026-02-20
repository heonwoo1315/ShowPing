(async function() {
    try {
        const response = await axios.get("/api/live/live-info", {
            withCredentials: true
        });

        const data = response.data;

        if (data && data !== "") {
            streamInfo = true;
            streamNo = data.streamNo;

            // 1. 방송 제목 및 설명 (input 요소)
            const titleEl = document.getElementById("broadcastTitle");
            if (titleEl) titleEl.value = data.streamTitle || "";

            const descEl = document.getElementById("broadcastDesc");
            if (descEl) descEl.value = data.streamDescription || "";

            // 2. 상품 이미지 및 정보 (img, div 요소)
            const imgEl = document.querySelector(".product-img");
            if (imgEl) imgEl.src = data.productImg || "";

            const infoEl = document.querySelector(".product-info");
            if (infoEl) infoEl.id = data.productNo || "";

            // 3. 상품 이름 및 가격 (span/div 등 textContent 요소)
            const nameEl = document.querySelector(".product-name");
            if (nameEl) nameEl.textContent = data.productName || "";

            const priceEl = document.querySelector(".product-origin-price");
            if (priceEl) priceEl.textContent = data.productPrice || 0;

            // 4. 상품 할인율 (input 요소)
            const discountEl = document.getElementById("discountRate");
            if (discountEl) discountEl.value = data.productSale || 0;

        } else {
            // 데이터가 없을 때의 초기화 로직도 안전하게 처리
            const titleEl = document.getElementById("broadcastTitle");
            if (titleEl) titleEl.value = "";

            const descEl = document.getElementById("broadcastDesc");
            if (descEl) descEl.value = "";

            const imgEl = document.querySelector(".product-img");
            if (imgEl) imgEl.src = "";

            const infoEl = document.querySelector(".product-info");
            if (infoEl) infoEl.id = "";

            const nameEl = document.querySelector(".product-name");
            if (nameEl) nameEl.textContent = "";

            const priceEl = document.querySelector(".product-origin-price");
            if (priceEl) priceEl.textContent = 0;

            const discountEl = document.getElementById("discountRate");
            // HTML 구조에 따라 value 혹은 textContent 선택
            if (discountEl) {
                if (discountEl.tagName === "INPUT") discountEl.value = 0;
                else discountEl.textContent = 0;
            }
        }

    } catch (error) {
        console.error("데이터 로딩 오류", error);
    } finally {
        // [럭키비키 핵심] 데이터가 있든 없든, UI 세팅 중에 에러가 나든
        // 영상 로직(stream.js)을 깨우기 위해 이벤트는 무조건 발생시킵니다!
        const event = new CustomEvent('dataLoaded');
        window.dispatchEvent(event);
    }
})();