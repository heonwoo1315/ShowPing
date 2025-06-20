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
        } else {
            throw new Error("비로그인 상태");
        }
    } catch (error) {
        console.warn("로그인 상태 아님:", error);

        // ✅ 비로그인 상태 처리
        if (authButton && authIcon) {
            authButton.href = "/login";
            authIcon.src = "/img/icon/login.png";
        }
    }
});

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
