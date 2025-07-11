document.addEventListener("DOMContentLoaded", async () => {
    const authButton = document.getElementById("auth-button");
    const authIcon = document.getElementById("auth-icon");
    const adminMenu = document.getElementById("admin-menu");

    // 로그인 상태 확인을 위한 API 호출
    try {
        const res = await fetch("/api/auth/user-info", {
            credentials: "include" // HttpOnly 쿠키 포함
        });

        if (res.ok) {
            const data = await res.json();

            // ✅ 로그인 상태 처리
            if (authButton && authIcon) {
                authButton.href = "#";
                authIcon.src = "/img/icon/logout.png";
                authButton.addEventListener("click", (e) => {
                    e.preventDefault();
                    logout();
                });
            }

            // ✅ 관리자 메뉴 표시
            if (data.role === "ADMIN" && adminMenu) {
                adminMenu.hidden = false;
            }
        } else if (res.status === 401) {
            // 인증되지 않은 경우 로그인 페이지로 이동
            handleUnauthorized();
        } else {
            throw new Error("서버 오류");
        }
    } catch (error) {
        console.warn("로그인 상태 아님 또는 요청 실패", error);
        handleUnauthorized();
    }
});

function handleUnauthorized() {
    const pathname = window.location.pathname;

    // 로그인 페이지로 리다이렉트할 경로만 지정
    const protectedPaths = [
        "/watch/history",
        "/cart",
        "/payment",
        "/success",
        "/user",
        "/admin",
        "/product/product_payment"
    ];

    const shouldRedirect = protectedPaths.some(path => pathname.startsWith(path));

    if (shouldRedirect) {
        window.location.href = "/login";
    } else {
        // 로그인 상태 UI 초기화
        const authButton = document.getElementById("auth-button");
        const authIcon = document.getElementById("auth-icon");

        if (authButton && authIcon) {
            authButton.href = "/login";
            authIcon.src = "/img/icon/login.png";
        }
    }
}

// 로그아웃 요청
async function logout() {
    try {
        await fetch("/api/auth/logout", {
            method: "POST",
            credentials: "include" // 쿠키 포함
        });
        location.href = "/";
    } catch (e) {
        console.error("로그아웃 실패:", e);
        alert("로그아웃 실패");
    }
}
