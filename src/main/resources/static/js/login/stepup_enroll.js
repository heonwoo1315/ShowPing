// --- cookie helpers ---
function getCookie(name) {
    const m = document.cookie.split("; ").find(v => v.startsWith(name + "="));
    return m ? decodeURIComponent(m.split("=")[1]) : null;
}

// /api/csrf 가 Set-Cookie(XSRF-TOKEN) 해주니 한 번 호출해 쿠키를 보장
async function ensureCsrfCookie() {
    try { await fetch("/api/csrf", { credentials: "include" }); } catch (_) {}
}

// Authorization + CSRF 헤더 구성
function authzHeaders() {
    const h = { "Content-Type": "application/json" };
    // accessToken은 쿠키로 쓰는 프로젝트라면 쿠키에서, 아니면 localStorage에서
    const at = getCookie("accessToken") || localStorage.getItem("accessToken");
    if (at) h["Authorization"] = "Bearer " + at;
    const xsrf = getCookie("XSRF-TOKEN");
    if (xsrf) h["X-XSRF-TOKEN"] = xsrf;
    return h;
}

window.addEventListener("DOMContentLoaded", () => {
    document.getElementById("btnEnrollStart")?.addEventListener("click", async () => {
        try {
            await ensureCsrfCookie();

            const res = await fetch("/auth/mfa/enroll/invite/me", {
                method: "POST",
                headers: authzHeaders(),
                credentials: "same-origin"
            });

            // 서버가 정책상 302로 403 페이지로 보낼 수 있으니 처리
            if (res.redirected) { location.href = res.url; return; }

            const ct = (res.headers.get("content-type") || "").toLowerCase();
            if (!ct.includes("application/json")) {
                const text = await res.text().catch(() => "");
                Swal.fire({ icon: "error", title: "초대 발급 실패", text: `status ${res.status}` });
                console.debug(text);
                return;
            }
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                Swal.fire({ icon: "error", title: "초대 발급 실패", text: `${res.status} ${err?.message || ""}` });
                return;
            }

            const { inviteId } = await res.json();
            if (!inviteId) {
                Swal.fire({ icon: "error", title: "초대 발급 실패", text: "inviteId 없음" });
                return;
            }
            location.href = `/enroll_mobile.html?inviteId=${encodeURIComponent(inviteId)}`;
        } catch (e) {
            Swal.fire({ icon: "error", title: "오류", text: e.message || "단말 등록을 시작할 수 없습니다." });
        }
    });
});
