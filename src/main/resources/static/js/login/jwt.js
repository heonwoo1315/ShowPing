// jwt.js
// 인증 전용 axios 인스턴스 + 재발급 인터셉터 + CSRF 보장

// 1) 로그인 페이지 여부
const IS_LOGIN_PAGE = window.location.pathname.startsWith('/login');

// 2) CSRF 쿠키 보장 (최초 1회)
let _csrfReady = false;
window.ensureCsrfCookie = async function ensureCsrfCookie() {
    if (_csrfReady) return;
    try {
        await fetch('/api/csrf', { credentials: 'include' }); // 서버가 XSRF-TOKEN 쿠키를 셋업
        _csrfReady = true;
    } catch (_) {}
};

// 3) 인증 전용 axios 인스턴스
window.authApi = axios.create({
    baseURL: '/api/auth/',
    withCredentials: true,
    xsrfCookieName: 'XSRF-TOKEN',     // CookieCsrfTokenRepository 기본 쿠키명
    xsrfHeaderName: 'X-XSRF-TOKEN',   // SecurityConfig 허용 헤더와 일치
});

// 4) 요청 인터셉터
window.authApi.interceptors.request.use(async (config) => {
    config._retry = config._retry || false;
    if (!IS_LOGIN_PAGE) await window.ensureCsrfCookie();
    return config;
});

// 5) 동시 재발급 1회 보장용
let _refreshPromise = null;

// 6) 응답 인터셉터: AT 만료(ME014)일 때만 재발급
window.authApi.interceptors.response.use(
    r => r,
    async (error) => {
        const { config, response } = error;
        if (!response) throw error;

        const isReissue = ((config.baseURL||'') + (config.url||'')).includes('/api/auth/reissue');
        if (isReissue || config._skipRefresh) throw error;

        const code = response?.data?.code || response?.headers?.['x-error-code'];
        if (response.status === 401 && code === 'ME014' && !config._retry) {
            try {
                config._retry = true;
                await window.authApi.post('reissue', {}, { _skipRefresh: true });
                return window.authApi(config); // 원요청 1회 재시도
            } catch (e) {
                // 실패(ME004) → UI만 비로그인 전환 (강제 리다이렉트 X)
                if (typeof setAsLogin === 'function') {
                    const btn  = document.getElementById('auth-button');
                    const icon = document.getElementById('auth-icon');
                    setAsLogin(btn, icon);
                }
                throw e;
            }
        }
        throw error;
    }
);

// 7) 로그인 여부 확인 유틸 (헤더 UI 등에서 사용)
window.fetchUserInfo = async () => {
    try {
        const res = await window.authApi.get('user-info');
        return res.data; // {status:"SUCCESS", username, role}
    } catch {
        return null;
    }
};
