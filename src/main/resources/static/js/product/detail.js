document.addEventListener("DOMContentLoaded", function () {
    const productNo = window.location.pathname.split('/').pop(); // URL에서 productNo 추출
    loadProductDetail(productNo);
    loadProductReview(productNo);
});

let product = null;

function loadProductDetail(productNo) {
    axios.get(`/api/products/detail/${productNo}`)
        .then(response => {
            product = response.data;
            const productSale = product.productSale;
            const productDetail = document.getElementById('product-detail-page');

            const formattedPrice = product.productPrice.toLocaleString('ko-KR');
            const formattedFinalPrice = product.discountedPrice.toLocaleString('ko-KR');

            productDetail.innerHTML = `
                <div class="product-detail">
                    <img src="${product.productImg}" alt="${product.productName}" />
                    <div class="product-info">
                        <h1>${product.productName}</h1>
                        <div class="product-price" id="product-sale" style="font-size: 25px">
                            <p style="text-decoration: line-through">${formattedPrice}원</p>
                            <p style="color: red">${product.productSale} %</p>
                        </div>
                        <div class="product-price-final" id="product-price-final">
                            <p>${formattedFinalPrice}원</p>
                        </div>
                        <div class="purchase-section">
                            <div class="quantity-control">
                                <button id="decrease-btn">-</button>
                                <input type="text" id="quantity-input" value="1" readonly>
                                <button id="increase-btn">+</button>
                            </div>
                            <div class="purchase-buttons">
                                <button id="add-to-cart-btn">장바구니</button>
                                <button id="direct-btn">바로 결제</button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="promotion-banner">
                    <img src="${product.productDescript}" alt="상품 상세 설명 이미지" />
                </div>
            `;

            if (productSale === 0) {
                document.getElementById("product-sale").style.display = "none";
            }

            setupEventListeners(productNo);
        })
        .catch(error => {
            console.error("상품 상세 정보를 불러오는 중 오류 발생:", error);
        });
}

function loadProductReview(productNo) {
    axios.get(`/api/products/reviews/${productNo}`)
        .then(response => {
            const reviews = response.data;
            const productReviews = document.getElementById('product-review');

            if (reviews.length === 0) {
                productReviews.innerHTML = `
                    <div class="product-reviews">
                        <h2>상품 리뷰 ${generateStars(0)}</h2>
                        <p>아직 등록된 리뷰가 없습니다.</p>
                    </div>
                `;
                return;
            }

            const totalRating = reviews.reduce((sum, review) => sum + review.reviewRating, 0);
            const averageRating = (totalRating / reviews.length).toFixed(1);
            let reviewListHtml = '';

            reviews.forEach((review, index) => {
                reviewListHtml += `
                    <div class="review">
                        <h3>${review.memberName} 님 ${generateStars(review.reviewRating)}</h3>
                        <p>${review.reviewComment}</p>
                        <small>작성일: ${new Date(review.reviewCreateAt).toLocaleDateString()}</small>
                        <div class="review-image"></div>
                    </div>
                `;
            });

            productReviews.innerHTML = `
                <div class="product-reviews">
                    <h3>상품 리뷰 ${generateStars(parseFloat(averageRating))} (${averageRating}/5)</h3>
                    ${reviewListHtml}
                </div>
            `;
        })
        .catch(error => {
            console.error("리뷰 정보를 불러오는 중 오류 발생:", error);
        });
}

function setupEventListeners(productNo) {
    const quantityInput = document.getElementById("quantity-input");
    const decreaseBtn = document.getElementById("decrease-btn");
    const increaseBtn = document.getElementById("increase-btn");
    const addToCartBtn = document.getElementById("add-to-cart-btn");
    const directBtn = document.getElementById("direct-btn");

    let quantity = 1;

    decreaseBtn.addEventListener("click", () => {
        if (quantity > 1) {
            quantity--;
            quantityInput.value = quantity;
        }
    });

    increaseBtn.addEventListener("click", () => {
        quantity++;
        quantityInput.value = quantity;
    });

    // 장바구니 버튼
    addToCartBtn.addEventListener("click", async () => {
        try {
            await window.ensureCsrfCookie(); // 쿠키 보장

            // 로그인 정보 1회 조회
            const { data: member } = await axios.get('/api/carts/info', { withCredentials: true });
            const memberNo = member?.memberNo;
            if (!memberNo) throw new Error('NO_AUTH');

            // 403이면 내부에서 한 번만 토큰 갱신 후 재시도
            await window.csrfRetry(() =>
                axios.post(`/api/carts/add?memberNo=${memberNo}`, {
                    productNo: product.productNo,
                    quantity: parseInt(document.getElementById("quantity-input").value, 10) || 1
                })
            );

            Swal.fire({
                title: '장바구니에 추가되었습니다.',
                text: '장바구니로 이동하시겠습니까?',
                icon: 'success',
                showCancelButton: true,
                confirmButtonText: '이동',
                cancelButtonText: '취소'
            }).then((r) => { if (r.isConfirmed) location.href = '/cart'; });

        } catch (error) {
            // 401/403 등 인증 실패 공통 처리
            Swal.fire({
                title: "로그인 필요",
                text: "장바구니를 사용하려면 로그인해야 합니다.",
                icon: "warning",
                confirmButtonText: "로그인"
            }).then(() => location.href = "/login");
        }
    });

    // ✅ 바로 결제 버튼
    directBtn.addEventListener("click", async () => {
        try {
            const response = await axios.get("/api/carts/info", {
                withCredentials: true
            });

            const memberNo = response.data.memberNo;
            if (!memberNo) {
                throw new Error("로그인 정보 없음");
            }

            const currentQuantity = parseInt(document.getElementById("quantity-input").value);

            if (!product || !product.productNo || !product.productName) {
                console.error("❌ 상품 정보가 유효하지 않습니다.", product);
                alert("상품 정보를 불러오지 못했습니다. 다시 시도해주세요.");
                return;
            }

            const selectedItem = {
                productNo: product.productNo,
                name: product.productName,
                quantity: quantity,
                totalPrice: product.discountedPrice * currentQuantity
            };

            // 여기는 선택 상품을 로컬에 잠깐 저장
            sessionStorage.setItem("selectedItems", JSON.stringify([selectedItem]));

            window.location.href = "/payment";
        } catch (error) {
            Swal.fire({
                title: "로그인 필요",
                text: "결제를 하려면 먼저 로그인해야 합니다.",
                icon: "warning",
                confirmButtonText: "로그인"
            }).then(() => window.location.href = "/login");
        }
    });
}

function generateStars(reviewAverage) {
    let starsHTML = '';
    for (let i = 0; i < 5; i++) {
        if (i < Math.floor(reviewAverage)) {
            starsHTML += '<img src="/img/icon/fillStar.png" style="width: 20px; height: 20px;">';
        } else if (i < Math.ceil(reviewAverage) && reviewAverage % 1 !== 0) {
            starsHTML += '<img src="/img/icon/halfStar.png" style="width: 20px; height: 20px;">';
        } else {
            starsHTML += '<img src="/img/icon/emptyStar.png" style="width: 20px; height: 20px;">';
        }
    }
    return starsHTML;
}
