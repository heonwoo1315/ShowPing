document.addEventListener("DOMContentLoaded", function () {
    loadCategories();
    setAdminNav();
});

function loadCategories() {
    axios.get('/api/categories')
        .then(response => {
            const categories = response.data;
            const dropdownMenu = document.querySelector('.dropdown-menu');

            dropdownMenu.innerHTML = ''; // 초기화

            const listItem = document.createElement('li' );
            const link = document.createElement('a');
            link.href = `/category/0`;
            link.textContent = `전체`;
            listItem.appendChild(link);
            dropdownMenu.appendChild(listItem);

            dropdownMenu.append()

            categories.forEach(category => {
                const listItem = document.createElement('li' );
                const link = document.createElement('a');
                link.href = `/category/${category.categoryNo}`; // 해당 category로 이동
                link.textContent = category.categoryName;
                listItem.appendChild(link);
                dropdownMenu.appendChild(listItem);
            });
        })
        .catch(error => {
            console.error("카테고리를 불러오는 중 오류 발생:", error);
        });
}

function getRoleFromAccessToken() {
    const token = sessionStorage.getItem("accessToken");
    if (!token) return null;

    try {
        // JWT Payload 부분 디코딩
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.role; // 👈 role 확인 (ROLE_USER, ROLE_ADMIN)
    } catch (e) {
        console.error("AccessToken 디코딩 오류:", e);
        return null;
    }
}

function setAdminNav() {
    const role = getRoleFromAccessToken();
    if (role === 'ROLE_ADMIN') {
        document.getElementById('admin-menu').hidden = false;
    }
}

// 버튼 요소 가져오기
const scrollToTopButton = document.getElementById("scrollToTop");

// 스크롤 이벤트 추가
window.addEventListener("scroll", () => {
    if (window.scrollY > 200) {
        scrollToTopButton.classList.add("show");
        scrollToTopButton.classList.remove("hide");
    } else {
        scrollToTopButton.classList.add("hide");
        scrollToTopButton.classList.remove("show");
    }
});

// 버튼 클릭 이벤트
scrollToTopButton.addEventListener("click", () => {
    window.scrollTo({
        top: 0,           // 맨 위로 이동
        behavior: "smooth" // 부드럽게 스크롤
    });
});