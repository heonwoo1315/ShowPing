// 페이지가 로드된 후 실행
document.addEventListener("DOMContentLoaded", function () {
    const selectBtn = document.querySelector(".select-btn");    // + 선택 버튼
    const productModal = document.getElementById("productModal");   // 모달 영역
    const closeBtn = document.querySelector(".close");  // 닫기(X) 버튼
    const container = document.querySelector(".product-list-container");    // 상품 목록 영역
    const productList = productModal.querySelector(".product-list");    // 상품 목록
    const loadingEl = document.getElementById("loading");   // 로딩 중
    const endMsgEl = document.getElementById("end-msg");    // 상품 목록 끝 메세지
    const productElement = document.querySelector(".product-element");  // 선택된 상품 영역

    let page = 0;
    const size = 20;
    let loading = false;
    let hasNext = true;

    // 모달 닫기
    closeBtn.addEventListener("click", () => {
        productModal.style.display = "none";
    });

    // 모달 열기 & 초기 데이터 로딩
    selectBtn.addEventListener("click", () => {
        page = 0;
        hasNext = true;
        productList.innerHTML = "";
        endMsgEl.style.display = "none";

        productModal.style.display = "block";
        loadMore(); // 첫 페이지 로딩
    });

    // 모달 스크롤 감지
    container.addEventListener("scroll", () => {
        const { scrollTop, clientHeight, scrollHeight } = container;

        if (!loading && hasNext && scrollTop + clientHeight >= scrollHeight - 10) {
            loadMore();
        }
    });

    async function loadMore() {
        if (loading || !hasNext) return;

        loading = true;
        loadingEl.style.display = "block";

        try {
            const response = await axios.get("/api/live/product/list", {
                params: {page, size}
            });

            const products = response.data.content || response.data;

            // 상품 추가
            products.forEach(product => {
                const li = document.createElement("li");
                li.className = "product-item";
                li.id = product.productNo;

                li.innerHTML = `
                    <img src="${product.productImg}" alt="상품 이미지">
                    <div class="product-info">
                        <div class="product-name">${product.productName}</div>
                        <div class="price-container">
                            <span class="original-price">${Number(product.productPrice).toLocaleString()}원</span>
                        </div>
                    </div>
                `;

                // 클릭 핸들러 (위임 대신 여기서 개별 등록)
                li.addEventListener("click", () => selectProduct(li));

                productList.appendChild(li);
            });

            // Pageable 응답인 경우 Pageable의 last 사용
            if (response.data.last !== undefined) {
                hasNext = !response.data.last;
            } else {
                hasNext = products.length === size;
            }
            page++;

            if (!hasNext) {
                endMsgEl.style.display = "block";
            }
        } catch (err) {
            console.log(err);
        } finally {
            loading = false;
            loadingEl.style.display = "none";
        }
    }

    function selectProduct(item) {
        const imgSrc = item.querySelector("img").src;
        const name = item.querySelector(".product-name").textContent.trim();
        const originalPrice = item.querySelector(".original-price").textContent.trim();
        const productNo = item.id;

        // 선택된 상품 표시 영역 내부를 동적으로 구성
        productElement.innerHTML = `
            <img src="${imgSrc}" alt="상품 이미지" class="product-img">
            <div class="product-info" id="${productNo}">
                <p class="product-name">${name}</p>
                <div class="product-price-container">
                    <p class="product-origin-price">${originalPrice}</p>
                </div>
            </div>
        `;

        productModal.style.display = "none";
    }

});