// 상품 클릭 시 상품 상세정보 페이지로 이동
document.addEventListener("DOMContentLoaded", function () {
    const productNo = window.location.pathname.split('/').pop(); // URL에서 productNo 추출
    loadProductDetail(productNo);
    loadProductReview(productNo);
});

let product = null

function loadProductDetail(productNo) {
    axios.get(`/api/products/detail/${productNo}`)
        .then(response => {
            product = response.data;
            const productSale = product.productSale;  // productSale 값을 가져옵니다.
            const productDetail = document.getElementById('product-detail-page');

            const formattedPrice = product.productPrice.toLocaleString('ko-KR'); // 가격 콤마 포맷팅
            const formattedFinalPrice = product.discountedPrice.toLocaleString('ko-KR');

            console.log(product)

            // 상품 상세 정보를 동적으로 삽입
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

                <!-- 상품 상세 설명 이미지 추가 -->
                <div class="promotion-banner">
                    <img src="${product.productDescript}" alt="상품 상세 설명 이미지" />
                </div>
            `;

            // productSale이 0일 경우 product-price를 숨기기
            if (productSale === 0) {
                document.getElementById("product-sale").style.display = "none"; // product-price 숨기기
            } else {
                document.getElementById("product-sale").style.display = "block"; // product-price 보이기
            }

            setupEventListeners(productNo); // 수량 조절 및 장바구니 추가 기능 연결

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
            // 리뷰가 비어있는지 확인
            if (reviews.length === 0) {
                productReviews.innerHTML = `
                    <div class="product-reviews">
                        <h2>상품 리뷰 ${generateStars(0)}</h2>
                        <p>아직 등록된 리뷰가 없습니다.</p>
                    </div>
                `;
            } else {
                // ★ 평균 별점 계산
                const totalRating = reviews.reduce((sum, review) => sum + review.reviewRating, 0);
                const averageRating = (totalRating / reviews.length).toFixed(1); // 소수점 1자리까지

                // 리뷰 리스트 생성
                let reviewListHtml = '';

                reviews.forEach((review, index) => {
                    reviewListHtml += `
                        <div class="review">
                            <h3>${review.memberName} 님 ${generateStars(review.reviewRating)}</h3>
                            <p>${review.reviewComment}</p>
                            <small>작성일: ${new Date(review.reviewCreateAt).toLocaleDateString()}</small>
                            <div class="review-image">
                                <!-- <img src="/img/${review.reviewUrl}.jpg" alt="리뷰 이미지 ${index + 1}" /> -->
                            </div>
                        </div>
                    `;
                });

                // 전체 리뷰 섹션 생성 (평균 별점에 generateStars 함수 적용)
                productReviews.innerHTML = `
                    <div class="product-reviews">
                        <h3>상품 리뷰 ${generateStars(parseFloat(averageRating))} (${averageRating}/5)</h3>
                        ${reviewListHtml}
                    </div>
                `;
            }
        })
        .catch(error => {
            console.error("상품 상세 정보를 불러오는 중 오류 발생:", error);
        });
}

function setupEventListeners(productNo) {
    const quantityInput = document.getElementById("quantity-input");
    const decreaseBtn = document.getElementById("decrease-btn");
    const increaseBtn = document.getElementById("increase-btn");
    const addToCartBtn = document.getElementById("add-to-cart-btn");
    const directBtn = document.getElementById("direct-btn");

    let quantity = 1;  // 기본 수량

    // - 버튼 클릭 시 수량 감소 (최소 1)
    decreaseBtn.addEventListener("click", function () {
        if (quantity > 1) {
            quantity--;
            quantityInput.value = quantity;
        }
    });

    // + 버튼 클릭 시 수량 증가
    increaseBtn.addEventListener("click", function () {
        quantity++;
        quantityInput.value = quantity;
    });

    // 장바구니 버튼 클릭 시 상품 추가 요청
    addToCartBtn.addEventListener("click", async function () {
        try {
            // JWT 토큰 가져오기 (sessionStorage 사용)
            const token = sessionStorage.getItem("accessToken");

            if (!token) {
                Swal.fire({
                    title: '로그인 필요',
                    text: '장바구니를 사용하려면 로그인해야 합니다. 로그인 하시겠습니까?',
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonText: '로그인',
                    cancelButtonText: '취소'
                }).then((result) => {
                    if (result.isConfirmed) {
                        // 사용자가 '로그인' 버튼을 클릭했을 때
                        window.location.href = "/login";  // 로그인 페이지로 이동
                    } else {
                        // 사용자가 '취소' 버튼을 클릭했을 때
                        console.log("사용자가 로그인하지 않기로 했습니다.");
                    }
                });
                return;
            }

            const response = await axios.get("/api/carts/info", {
                withCredentials: true
            });

            console.log(response.data);

            const memberNo = response.data.memberNo;  // 로그인된 사용자 정보에서 memberNo 추출

            // 로그인 여부 확인
            if (!memberNo) {
                alert("사용자 정보가 없습니다.");
                return;
            }

            // 장바구니에 상품 추가 요청
            axios.post(`/api/carts/add?memberNo=${memberNo}`, {
                productNo: productNo,
                quantity: quantity
            })
                .then(response => {
                    quantityInput.value = 1;  // 수량 초기화
                    Swal.fire({
                        title: '장바구니에 추가되었습니다.',
                        text: '장바구니로 이동하시겠습니까?',
                        icon: 'success',
                        showCancelButton: true,
                        confirmButtonText: '이동',
                        cancelButtonText: '취소'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            // 사용자가 '이동' 버튼을 클릭했을 때
                            window.location.href = "/cart";  // 장바구니 페이지로 이동
                        } else {
                            // 사용자가 '취소' 버튼을 클릭했을 때
                            console.log("사용자가 상품페이지에 머물기로 했습니다.");
                        }
                    });
                    return;
                })
                .catch(error => {
                    alert("장바구니 추가 실패: " + (error.response?.data || "알 수 없는 오류"));
                });
        } catch (error) {
            console.error("로그인 이후 장바구니를 사용할 수 있습니다.", error);
            if (confirm("로그인 이후 장바구니를 사용할 수 있습니다. 로그인 하시겠습니까?")) {
                window.location.href = "/login";  // 로그인 페이지로 이동
            }
        }
    });

    directBtn.addEventListener("click", function () {

        if (!token) {
            Swal.fire({
                title: '로그인 필요',
                text: '장바구니를 사용하려면 로그인해야 합니다. 로그인 하시겠습니까?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: '로그인',
                cancelButtonText: '취소'
            }).then((result) => {
                if (result.isConfirmed) {
                    // 사용자가 '로그인' 버튼을 클릭했을 때
                    window.location.href = "/login";  // 로그인 페이지로 이동
                } else {
                    // 사용자가 '취소' 버튼을 클릭했을 때
                    console.log("사용자가 로그인하지 않기로 했습니다.");
                }
            });
            return;
        }

        const selectedItem = {
            productNo: product.productNo,
            name: product.productName,
            quantity: quantity,
            totalPrice: product.discountedPrice * quantity
        };

        // sessionStorage에 상품 정보 저장
        sessionStorage.setItem("selectedItems", JSON.stringify([selectedItem]));

        // 결제 페이지로 이동
        window.location.href = "/payment";
    });
}

// 별점 생성 함수
function generateStars(reviewAverage) {
    let starsHTML = '';
    for (let i = 0; i < 5; i++) {
        if (i < Math.floor(reviewAverage)) {
            starsHTML += '<img src="/img/icon/fillStar.png" alt="filled star" style="width: 20px; height: 20px;">';
        } else if (i < Math.ceil(reviewAverage) && reviewAverage % 1 !== 0) {
            starsHTML += '<img src="/img/icon/halfStar.png" alt="empty star" style="width: 20px; height: 20px;">';
        }
        else {
            starsHTML += '<img src="/img/icon/emptyStar.png" alt="empty star" style="width: 20px; height: 20px;">';
        }
    }
    return starsHTML;
}