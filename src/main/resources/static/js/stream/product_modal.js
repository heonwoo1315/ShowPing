// 페이지가 로드된 후 실행
document.addEventListener("DOMContentLoaded", function() {
    const selectBtn = document.querySelector(".select-btn");    // + 선택 버튼
    const productModal = document.getElementById("productModal");   // 모달 영역
    const closeBtn = document.querySelector(".close");  // 닫기(X) 버튼
    const productElement = document.querySelector(".product-element");  // 선택된 상품 표시 영역

    // 1) + 선택 버튼을 누르면 모달 열기
    selectBtn.addEventListener("click", function() {
        axios.get("/api/live/product/list")
            .then((response) => {
                // API를 통해 받은 상품 목록
                const products = response.data;

                // <ul> 태그
                const productList = document.querySelector(".product-list");
                // 기존에 있던 목록 초기화
                productList.innerHTML = '';

                // 목록의 각 요소에 대해 <li> 태그 생성
                products.forEach(product => {
                    // <li> 태그 생성 및 속성 설정
                    const li = document.createElement('li');
                    li.className = 'product-item';
                    li.id = product.productNo;

                    // <img> 태그 생성 및 속성 설정
                    const img = document.createElement('img');
                    img.src = product.productImg;
                    img.alt = '상품 이미지';

                    // 상품 정보 컨테이너 생성
                    const productInfo = document.createElement('div');
                    productInfo.className = 'product-info';

                    // 상품 이름 <div> 태그 생성 및 속성, 내용 설정
                    const productNameDiv = document.createElement('div');
                    productNameDiv.className = 'product-name';
                    productNameDiv.textContent = product.productName;

                    // 가격 컨테이너 설정
                    const priceContainer = document.createElement('div');
                    priceContainer.className = 'price-container';

                    // 가격 <span> 태그 생성 및 내용 설정
                    const priceSpan = document.createElement('span');
                    priceSpan.className = 'original-price';
                    priceSpan.textContent = Number(product.productPrice).toLocaleString() + '원';

                    // 'price-container'에 'original-price' span 추가
                    priceContainer.appendChild(priceSpan);

                    // 'product-info'에 'product-name' div와 'price-container' 추가
                    productInfo.appendChild(productNameDiv);
                    productInfo.appendChild(priceContainer);

                    // li에 img와 'product-info' 추가
                    li.appendChild(img);
                    li.appendChild(productInfo);

                    // ul 리스트에 li 추가
                    productList.appendChild(li);

                    // 최종 모습
                    // <ul class="product-list">
                    //     <li class="product-item" id="productNo">
                    //         <img src="https://~~~~" alt="상품 이미지" />
                    //         <div class="product-info">
                    //             <div class="product-name">상품명</div>
                    //             <div class="price-container">
                    //                 <span class="original-price">가격</span>
                    //             </div>
                    //         </div>
                    //     </li>
                    // </ul>
                });

                const productItems = document.querySelectorAll(".product-item");    // 상품 목록
                // 4) 각 상품 클릭 시, 선택된 상품 정보 표시 후 모달 닫기
                productItems.forEach((item) => {
                    item.addEventListener("click", () => {
                        // 선택된 상품의 정보 추출
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
                    });
                })
            })
            .catch((error) => {
                console.log("상품 정보를 가져오는데 실패했습니다." + error);
            });

        productModal.style.display = "block";
    });

    // 2) 닫기 버튼(X)을 누르면 모달 닫기
    closeBtn.addEventListener("click", function() {
        productModal.style.display = "none";
    });

    // 3) 모달 영역 바깥을 클릭하면 모달 닫기
    window.addEventListener("click", function(e) {
        if (e.target === productModal) {
            productModal.style.display = "none";
        }
    });


});
