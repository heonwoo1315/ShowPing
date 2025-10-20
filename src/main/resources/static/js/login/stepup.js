// ====== DOM ======
const totpInput  = document.getElementById('totp');
const btnVerify  = document.getElementById('btnVerify');
const btnCancel  = document.getElementById('btnCancel');
const btnEnroll  = document.getElementById('btnEnroll'); // 있으면 사용

// ====== 설정 ======
const LOGOUT_URL = '/api/auth/logout'; // 실제 로그아웃 API 경로에 맞게 필요시 수정

// ====== 유틸 ======
function setMsg(text) {
    const el = document.getElementById('msg');
    if (el) el.textContent = text || '';
}

function getCookie(name) {
    const m = document.cookie.match('(?:^|; )' + name.replace(/([.$?*|{}()[\]\\/+^])/g, '\\$1') + '=([^;]*)');
    return m ? decodeURIComponent(m[1]) : null;
}

function authzHeaders() {
    const xsrf = getCookie('XSRF-TOKEN');
    return {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
        ...(xsrf ? { 'X-XSRF-TOKEN': xsrf } : {})
    };
}

// base64url <-> bytes
function b64uToBytes(b64url) {
    if (!b64url) return new Uint8Array();
    let b64 = b64url.replace(/-/g, '+').replace(/_/g, '/');
    const pad = b64.length % 4;
    if (pad) b64 += '='.repeat(4 - pad);
    const str = atob(b64);
    const out = new Uint8Array(str.length);
    for (let i = 0; i < str.length; i++) out[i] = str.charCodeAt(i);
    return out;
}
function b64urlEnc(buf) {
    const bytes = buf instanceof ArrayBuffer ? new Uint8Array(buf) : new Uint8Array(buf.buffer, buf.byteOffset, buf.byteLength);
    let s = '';
    for (let i = 0; i < bytes.length; i++) s += String.fromCharCode(bytes[i]);
    return btoa(s).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
}

// ====== WebAuthn 옵션 변환/호출 ======
function toAssertionPublicKey(opts) {
    const allow = Array.isArray(opts.allowCredentials) ? opts.allowCredentials : [];
    return {
        challenge: b64uToBytes(opts.challenge),
        rpId: opts.rpId,
        userVerification: opts.userVerification || 'preferred',
        timeout: opts.timeout || 60000,
        allowCredentials: allow.map(d => ({
            type: d.type || 'public-key',
            id: (d.id instanceof ArrayBuffer || ArrayBuffer.isView(d.id)) ? d.id : b64uToBytes(String(d.id || '')),
            transports: d.transports
        }))
    };
}

async function fetchAssertionOptions() {
    const res = await fetch('/auth/mfa/assertion/options', {
        method: 'GET',
        headers: authzHeaders(),
        credentials: 'same-origin'
    });
    if (!res.ok) throw new Error('인증 옵션 로드 실패: ' + res.status);
    const serverOpts = await res.json();
    return toAssertionPublicKey(serverOpts);
}

async function doWebAuthnAssert(publicKey) {
    if (!('credentials' in navigator) || !('get' in navigator.credentials)) {
        throw new Error('이 브라우저는 WebAuthn을 지원하지 않습니다.');
    }
    const cred = await navigator.credentials.get({ publicKey });
    return {
        rawId:            b64urlEnc(cred.rawId),
        authenticatorData:b64urlEnc(cred.response.authenticatorData),
        clientDataJSON:   b64urlEnc(cred.response.clientDataJSON),
        signature:        b64urlEnc(cred.response.signature),
        userHandle:       cred.response.userHandle ? b64urlEnc(cred.response.userHandle) : null
    };
}

// ====== 조용한 로그아웃(취소/필요 시 공용) ======
function logoutSilent(reason = 'mfaCanceled') {
    try {
        const xsrf = getCookie('XSRF-TOKEN');
        fetch(LOGOUT_URL, {
            method: 'POST',
            credentials: 'include',
            keepalive: true, // 네비게이션 중에도 서버로 전달
            headers: {
                'Content-Type': 'application/json',
                ...(xsrf ? { 'X-XSRF-TOKEN': xsrf } : {})
            },
            body: '{}'
        }).catch(() => {});
    } catch (_) {}
    try { localStorage.removeItem('accessToken'); } catch {}
    try { localStorage.removeItem('refreshToken'); } catch {}
    location.replace('/login?' + encodeURIComponent(reason));
}

// ====== 이벤트 바인딩 ======
if (btnVerify) {
    btnVerify.addEventListener('click', async () => {
        try {
            setMsg('');
            const totp = (totpInput?.value || '').trim();
            if (!/^\d{6}$/.test(totp)) {
                setMsg('TOTP 6자리를 입력하세요.');
                totpInput?.focus();
                return;
            }
            const publicKey = await fetchAssertionOptions();
            const assertion = await doWebAuthnAssert(publicKey);

            const res = await fetch('/auth/mfa/verify', {
                method: 'POST',
                headers: authzHeaders(),
                credentials: 'same-origin',
                body: JSON.stringify({ totp, ...assertion })
            });
            if (!res.ok) throw new Error('2차 인증 실패: ' + res.status);

            const data = await res.json();
            if (data && data.ok) {
                location.replace(data.redirect || '/admin/');
            } else {
                setMsg(data?.message || '인증 실패');
            }
        } catch (e) {
            console.error(e);
            setMsg(e.message || '처리 중 오류가 발생했습니다.');
        }
    });
}

if (btnCancel) {
    btnCancel.addEventListener('click', () => logoutSilent('mfaCanceled'));
}

if (btnEnroll) {
    btnEnroll.addEventListener('click', () => {
        // 필요 시 등록 페이지로 이동
        location.href = '/enroll_mobile.html';
    });
}

// ====== 뒤로가기 누르면 현재 페이지에 머무르게 ======
(function keepHere() {
    try { history.scrollRestoration = 'manual'; } catch {}

    const url = location.href;
    history.replaceState({ stay: true }, '', url);
    history.pushState({ stay: true }, '', url);

    window.addEventListener('popstate', function () {
        // 일부 브라우저에서 즉시 호출 무시 방지: 한 틱 지연
        setTimeout(() => history.go(1), 0);
    });

    // BFCache 복귀 시 장벽 재무장
    window.addEventListener('pageshow', function (e) {
        if (e.persisted) {
            history.replaceState({ stay: true }, '', url);
            history.pushState({ stay: true }, '', url);
        }
    });

    // (선택) 입력 중이 아닐 때 Backspace로 뒤로가기 되는 환경 차단
    document.addEventListener('keydown', (e) => {
        const t = e.target;
        const typing = t && (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA' || t.isContentEditable);
        if (!typing && e.key === 'Backspace') e.preventDefault();
    });
})();
