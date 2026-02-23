// 1. signup.js 등에서 호출하는 공통 함수 정의
window.__getCsrfToken = function() {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    return tokenMeta ? tokenMeta.getAttribute('content') : '';
};

// 2. DOM이 로드된 후 Axios 전역 설정 적용
document.addEventListener("DOMContentLoaded", function() {
    const headerNameMeta = document.querySelector('meta[name="_csrf_header"]');

    if (headerNameMeta && window.axios) {
        const headerName = headerNameMeta.getAttribute('content');
        const token = window.__getCsrfToken();

        // 모든 Axios 요청의 헤더에 CSRF 토큰 자동 포함
        axios.defaults.headers.common[headerName] = token;

        console.log("CSRF Security properly initialized for Axios.");
    }
});