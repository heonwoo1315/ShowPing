// csrf-setup.js (clean + robust)
//
// 핵심:
// - XSRF-TOKEN 쿠키가 없으면 먼저 /api/csrf 로 "토큰 발급"을 워밍업
// - 상태변경 요청(POST/PUT/PATCH/DELETE)은 항상 X-XSRF-TOKEN 헤더를 붙임
// - 403이면 1회 강제 재발급 후 자동 재시도
//
// 주의:
// - accessToken/refreshToken이 HttpOnly면 JS에서 못 읽는 게 정상.
//   그래서 Authorization 헤더를 억지로 만들지 말고, withCredentials 쿠키 인증에 맡김.

(function () {
    const CSRF_COOKIE = 'XSRF-TOKEN';
    const CSRF_HEADER = 'X-XSRF-TOKEN';

    // 필요하면 여기서 호스트 강제 통일(선택)
    // 예: www로 접속하면 non-www로 보내기
    // if (location.hostname === 'www.showping-live.com') {
    //   location.replace('https://showping-live.com' + location.pathname + location.search + location.hash);
    // }

    function getCookie(name) {
        const cookies = document.cookie ? document.cookie.split('; ') : [];
        for (const c of cookies) {
            const eq = c.indexOf('=');
            const k = eq >= 0 ? c.slice(0, eq) : c;
            if (k === name) return decodeURIComponent(eq >= 0 ? c.slice(eq + 1) : '');
        }
        return null;
    }

    function isSafeMethod(method) {
        const m = (method || 'GET').toUpperCase();
        return m === 'GET' || m === 'HEAD' || m === 'OPTIONS' || m === 'TRACE';
    }

    // 동시에 여러 요청이 와도 /api/csrf 호출은 1번만 돌게
    let csrfInitPromise = null;

    async function ensureCsrfCookie({ force = false } = {}) {
        const existing = getCookie(CSRF_COOKIE);
        if (existing && !force) return existing;

        if (csrfInitPromise && !force) return csrfInitPromise;

        csrfInitPromise = (async () => {
            // /api/csrf 엔드포인트가 없어도, 보안필터를 타면 토큰 쿠키가 생길 수도 있음.
            // 그래도 성공 여부는 "쿠키가 생겼는지"로 판단.
            try {
                const res = await fetch('/api/csrf', {
                    method: 'GET',
                    credentials: 'include',
                    cache: 'no-store',
                    headers: { 'X-CSRF-INIT': '1' },
                });

                // 디버그용(원하면 로그 유지)
                if (!res.ok) {
                    // 404라도 쿠키가 생기면 괜찮음(보안 필터가 발급해준 케이스)
                    // console.warn('[csrf] /api/csrf responded', res.status);
                }
            } catch (e) {
                // 네트워크 에러 등
                // console.warn('[csrf] init fetch failed', e);
            }

            // 브라우저가 Set-Cookie 반영하는 데 아주 짧은 틱이 필요한 경우가 있어서 한 번 양보
            await new Promise((r) => setTimeout(r, 0));

            const token = getCookie(CSRF_COOKIE);
            if (!token) {
                // 여기까지 왔는데도 쿠키가 없으면: (1) 호스트 불일치(www/non-www) (2) 서버가 쿠키를 HttpOnly로 내림 (3) 보안설정 문제
                throw new Error('[csrf] XSRF-TOKEN cookie is still missing after init');
            }
            return token;
        })();

        try {
            return await csrfInitPromise;
        } finally {
            csrfInitPromise = null;
        }
    }

    // axios 기본 설정
    if (typeof axios !== 'undefined') {
        axios.defaults.withCredentials = true;
        axios.defaults.xsrfCookieName = CSRF_COOKIE;
        axios.defaults.xsrfHeaderName = CSRF_HEADER;

        // 요청 인터셉터: 상태변경 요청이면 CSRF 토큰 보장 + 헤더 부착
        axios.interceptors.request.use(async (config) => {
            const method = (config.method || 'GET').toUpperCase();

            if (!isSafeMethod(method)) {
                // 토큰이 없으면 먼저 발급
                await ensureCsrfCookie({ force: false });

                const token = getCookie(CSRF_COOKIE);
                config.headers = config.headers || {};
                if (token) config.headers[CSRF_HEADER] = token;
            }

            config.withCredentials = true;
            return config;
        });

        // 응답 인터셉터: 403이면 1회 강제 재발급 후 재시도
        axios.interceptors.response.use(
            (res) => res,
            async (err) => {
                const status = err?.response?.status;
                const config = err?.config;

                if (!config) throw err;

                const method = (config.method || 'GET').toUpperCase();
                const canRetry = status === 403 && !isSafeMethod(method) && !config.__csrfRetried;

                if (!canRetry) throw err;

                config.__csrfRetried = true;

                // 강제로 새 토큰 발급 후 재시도
                await ensureCsrfCookie({ force: true });
                const token = getCookie(CSRF_COOKIE);

                config.headers = config.headers || {};
                if (token) config.headers[CSRF_HEADER] = token;

                return axios(config);
            }
        );
    }

    // window로 노출(기존 stream.js 호환)
    window.getCookie = getCookie;
    window.ensureCsrfCookie = ensureCsrfCookie;

    window.csrfRequest = async function (cfg) {
        // axios 인터셉터가 알아서 처리하지만,
        // 외부에서 직접 쓸 때도 안전하게 보장
        const method = (cfg?.method || 'GET').toUpperCase();
        if (!isSafeMethod(method)) {
            await ensureCsrfCookie({ force: false });
            const token = getCookie(CSRF_COOKIE);
            cfg.headers = cfg.headers || {};
            if (token) cfg.headers[CSRF_HEADER] = token;
        }
        cfg.withCredentials = true;
        return axios(cfg);
    };

    window.csrfPost = (url, data, cfg = {}) =>
        window.csrfRequest({ ...cfg, method: 'POST', url, data });

    window.csrfPut = (url, data, cfg = {}) =>
        window.csrfRequest({ ...cfg, method: 'PUT', url, data });

    window.csrfPatch = (url, data, cfg = {}) =>
        window.csrfRequest({ ...cfg, method: 'PATCH', url, data });

    window.csrfDelete = (url, cfg = {}) =>
        window.csrfRequest({ ...cfg, method: 'DELETE', url });
})();
