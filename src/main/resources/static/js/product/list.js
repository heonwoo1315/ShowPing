let currentPage = 0;
const itemsPerPage = 8;
const categoryNo = window.location.pathname.split('/').pop();
let sortOption = "quantity-desc"; // 기본 정렬 기준

document.addEventListener("DOMContentLoaded", function () {
    loadProducts();
    setupSortButtons();
});

document.getElementById('load-more').addEventListener('click', loadProducts);

// 정렬 버튼 클릭 시
function setupSortButtons() {
    const buttons = document.querySelectorAll('.sort-btn');
    buttons.forEach(button => {
        button.addEventListener('click', function () {
            // 클릭된 버튼에 'selected' 클래스를 추가하고 나머지 버튼에서 제거
            buttons.forEach(btn => btn.classList.remove('selected'));
            button.classList.add('selected');

            // 정렬 기준 업데이트
            sortOption = button.id.replace('sort-', ''); // 예: 'price-asc', 'price-desc' 등
            currentPage = 0;
            document.getElementById('product-grid').innerHTML = ''; // 기존 상품 리스트 비우기
            loadProducts();
        });
    });
}

function loadProducts() {
    axios.get(`/api/products/${categoryNo}?page=${currentPage}&size=${itemsPerPage}&sort=${sortOption}`)
        .then(response => {
            const products = response.data.content;
            renderProducts(products);
            currentPage++;

            if (response.data.last) {
                document.getElementById('load-more').style.display = 'none';
            } else {
                document.getElementById('load-more').style.display = 'block';
            }
        })
        .catch(error => {
            console.error("상품 목록을 불러오는 중 오류 발생:", error);
        });
}

function renderProducts(products) {
    const productGrid = document.getElementById('product-grid');

    products.forEach(product => {
        const productDiv = document.createElement('div');
        productDiv.classList.add('product-item');
        const formattedPrice = product.productPrice.toLocaleString('ko-KR');
        const formattedFinalPrice = product.discountedPrice.toLocaleString('ko-KR');
        const productSale = product.productSale;
        const productImg = convertToWebP(product.productImg);

        function convertToWebP(imageUrl) {
            // .jpg를 .webp로 변경
            if (imageUrl.endsWith('.jpg')) {
                return imageUrl.replace('.jpg', '.webp');
            }
            return imageUrl; // 이미 .jpg가 아니면 그대로 반환
        }

        let productName = product.productName;
        if (productName.length > 25) {
            productName = productName.substring(0, 25) + '...';
        }

        productDiv.innerHTML = `
            <div class="product-img-container">
                <img id="product-sale-icon" src="/img/icon/sale.png" alt="product-sale" class="sale-icon" style="width: 50px" />
                <img src="${productImg}" alt="${productName}" class="product-img" />
            </div>
            <p class="product-name">${productName}</p>
            <p class="product-sale" id="product-sale" style="text-decoration: line-through; font-size: 15px">${formattedPrice}원</p>
            <p class="product-sale-percent" id="product-sale-percent" style="color: red; font-size: 15px">${product.productSale} %</p>
            <p class="product-price-final" id="product-price-final" style="font-size: 20px">${formattedFinalPrice}원</p>
        
            <!-- 리뷰 평균 별점 및 리뷰 개수 추가 -->
            <div class="product-review">
                <!-- 리뷰 별점 -->
                <div class="review-stars">
                    ${generateStars(product.reviewAverage)}
                    <p style="margin-bottom: 10px; margin-left: 5px; font-size: 15px" class="product-review-count">${product.reviewCount > 0 ? `(${product.reviewCount.toLocaleString()})` : ''}</p>
                </div>
            </div>
        `;

        if (product.productSale === 0) {
            productDiv.querySelector(".product-sale").style.display = "none";
            productDiv.querySelector("#product-sale-icon").style.display = "none";
            productDiv.querySelector("#product-sale-percent").style.display = "none";
        } else {
            productDiv.querySelector(".product-sale").style.display = "block";
            productDiv.querySelector("#product-sale-icon").style.display = "block";
            productDiv.querySelector("#product-sale-percent").style.display = "block";
        }

        // 리뷰 개수가 0일 경우 리뷰 섹션 숨기기
        if (product.reviewCount === 0) {
            productDiv.querySelector(".product-review").style.display = "none";  // 리뷰 섹션을 숨김
        }

        productDiv.addEventListener('click', () => {
            window.location.href = `/product/detail/${product.productNo}`;
        });

        productGrid.appendChild(productDiv);
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
