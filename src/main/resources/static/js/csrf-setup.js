if (window.axios) {
    axios.defaults.withCredentials = true;
    axios.defaults.xsrfCookieName = 'XSRF-TOKEN';
    axios.defaults.xsrfHeaderName = 'X-XSRF-TOKEN';
}

window.csrfRequest = async function(method, url, data) {
    await window.ensureCsrfCookie(); // 쿠키 보장
    // axios 인터셉터가 X-XSRF-TOKEN 자동 주입
    try {
        return await axios({ method, url, data });
    } catch (err) {
        if (err.response && err.response.status === 403) { // 403에러가 날 경우 (기대 효과 UX향상)
            await window.ensureCsrfCookie();             // 토큰 재발급
            return await axios({ method, url, data });   // 1회 재시도
        }
        throw err;
    }
};

document.addEventListener('DOMContentLoaded', async () => {
    if (!document.cookie.split('; ').some(c => c.startsWith('XSRF-TOKEN='))) {
        try { await fetch('/api/csrf', { credentials: 'include' }); } catch {}
    }
});

;(function (global) {
    // --- 공통: 토큰 읽기 ---
    function getCsrfToken() {
        var m = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
        return m ? decodeURIComponent(m[1]) : null;
    }

    // fetch 등에서 필요할 때 사용할 수 있도록 노출
    global.__getCsrfToken = getCsrfToken;

    // --- axios 전역(또는 개별 인스턴스)용 설정 함수 ---
    function setupCsrfOnAxios(axiosInstance) {
        if (!axiosInstance) return;

        // 이미 장착되어 있다면 중복 등록 막기
        if (axiosInstance.__csrfInterceptorInstalled) return;

        axiosInstance.defaults.withCredentials = true;

        var id = axiosInstance.interceptors.request.use(function (config) {
            var method = (config && config.method ? String(config.method) : 'get').toLowerCase();
            if (method === 'post' || method === 'put' || method === 'delete' || method === 'patch') {
                var token = getCsrfToken();
                if (token) {
                    config.headers = config.headers || {};
                    if (!('X-XSRF-TOKEN' in config.headers)) {
                        config.headers['X-XSRF-TOKEN'] = token;
                    }
                }
            }
            return config;
        });

        axiosInstance.__csrfInterceptorInstalled = true;
        axiosInstance.__csrfInterceptorId = id;
    }

    window.ensureCsrfCookie = async function ensureCsrfCookie() {
        const hasCookie = document.cookie.split('; ').some(c => c.startsWith('XSRF-TOKEN='));
        if (!hasCookie) {
            // 서버에서 XSRF-TOKEN 쿠키만 발급받는 엔드포인트(permitAll)
            await fetch('/api/csrf', { credentials: 'include' });
        }
    };

    // 전역 axios 자동 장착 (있을 때만)
    if (typeof window !== 'undefined' && window.axios) {
        setupCsrfOnAxios(window.axios);
    } else {
        window.addEventListener('load', () => window.axios && setupCsrfOnAxios(window.axios));
    }

    // --- fetch 유틸 (선택) ---
    function csrfFetch(input, init) {
        init = init || {};
        var method = ((init.method || 'GET') + '').toUpperCase();

        if (method === 'POST' || method === 'PUT' || method === 'DELETE' || method === 'PATCH') {
            var token = getCsrfToken();
            if (token) {
                init.headers = init.headers || {};
                // 기존 헤더가 Map/Headers 가 아니라 plain object 라는 가정(일반 케이스)
                if (!(init.headers['X-XSRF-TOKEN'] || init.headers['x-xsrf-token'])) {
                    init.headers['X-XSRF-TOKEN'] = token;
                }
            }
            // 쿠키 동봉
            if (init.credentials == null) {
                init.credentials = 'include';
            }
        }
        return fetch(input, init);
    }

    // 전역 노출(선택)
    global.setupCsrfOnAxios = setupCsrfOnAxios;
    global.csrfFetch = csrfFetch;

    // ESM/CommonJS 환경 지원(번들러 사용 시)
    if (typeof module !== 'undefined' && module.exports) {
        module.exports = { setupCsrfOnAxios, csrfFetch, __getCsrfToken: getCsrfToken };
    } else if (typeof define === 'function' && define.amd) {
        define(function () { return { setupCsrfOnAxios, csrfFetch, __getCsrfToken: getCsrfToken }; });
    }
})(typeof window !== 'undefined' ? window : this);

(function () {
    if (!window.axios || !window.setupCsrfOnAxios) return;

    // 1) 기본 전역 axios에도 장착
    window.setupCsrfOnAxios(window.axios);

    // 2) axios.create로 새 인스턴스를 만들 때마다 자동 장착
    const origCreate = window.axios.create;
    window.axios.create = function (config) {
        const instance = origCreate.call(window.axios, config);
        try { window.setupCsrfOnAxios(instance); } catch (_) {}
        return instance;
    };
})();

// 403이면 한 번만 CSRF 쿠키를 갱신하고 재시도
window.csrfRetry = async function csrfRetry(fn) {
    try {
        return await fn();
    } catch (err) {
        const status = err?.response?.status;
        if (status === 403) {
            try {
                await window.ensureCsrfCookie();          // 새 쿠키 발급
                return await fn();                        // 한 번만 재시도
            } catch (e2) {
                throw e2;
            }
        }
        throw err;
    }
};

// 편의: JSON POST 래퍼(선택)
window.csrfPost = (url, body) =>
    window.csrfRetry(() => axios.post(url, body));