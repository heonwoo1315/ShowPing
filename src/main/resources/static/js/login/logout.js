// logout.js
// 로그아웃 버튼 클릭 시 호출
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('logoutBtn'); // 네 버튼 id로 맞춰줘
    if (!btn) return;

    btn.addEventListener('click', async (e) => {
        e.preventDefault();
        doLogoutOnce();
    });
});
