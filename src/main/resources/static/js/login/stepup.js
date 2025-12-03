// /js/login/stepup.js
(() => {
    'use strict';

    // ===== DOM =====
    const totpInput = document.getElementById('totp');
    const btnVerify = document.getElementById('btnVerify');
    const btnCancel = document.getElementById('btnCancel');

    // ===== Utils =====
    function setMsg(text, ok = false) {
        const el = document.getElementById('msg');
        if (!el) return;
        el.textContent = text || '';
        el.style.color = ok ? '#065f46' : '#b91c1c';
    }
    function getCookie(name) {
        const m = document.cookie.split('; ').find(v => v.startsWith(name + '='));
        return m ? decodeURIComponent(m.split('=')[1]) : null;
    }
    function authzHeaders() {
        const xsrf = getCookie('XSRF-TOKEN');
        return {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest',
            ...(xsrf ? { 'X-XSRF-TOKEN': xsrf } : {})
        };
    }
    function b64uToBytes(b64url) {
        const pad = '='.repeat((4 - b64url.length % 4) % 4);
        const base64 = (b64url.replace(/-/g, '+').replace(/_/g, '/')) + pad;
        const bin = atob(base64);
        const buf = new Uint8Array(bin.length);
        for (let i = 0; i < bin.length; i++) buf[i] = bin.charCodeAt(i);
        return buf.buffer;
    }
    function bytesToB64u(buf) {
        const bytes = buf instanceof ArrayBuffer ? new Uint8Array(buf) : new Uint8Array(buf.buffer, buf.byteOffset, buf.byteLength);
        let s = '';
        for (let i = 0; i < bytes.length; i++) s += String.fromCharCode(bytes[i]);
        return btoa(s).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
    }

    // ===== WebAuthn 옵션 변환/호출 =====
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
            throw new Error('이 브라우저는 WebAuthn(인증)을 지원하지 않습니다.');
        }
        const cred = await navigator.credentials.get({ publicKey });
        const resp = cred.response;
        return {
            rawId: bytesToB64u(cred.rawId),
            authenticatorData: bytesToB64u(resp.authenticatorData),
            clientDataJSON: bytesToB64u(resp.clientDataJSON),
            signature: bytesToB64u(resp.signature),
            userHandle: resp.userHandle ? bytesToB64u(resp.userHandle) : null
        };
    }

    // ===== 로그아웃(취소) =====
    async function logoutSilent(reason = 'mfaCanceled') {
        try {
            const xsrf = getCookie('XSRF-TOKEN');
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest',
                    ...(xsrf ? { 'X-XSRF-TOKEN': xsrf } : {})
                },
                credentials: 'same-origin',
                body: '{}'
            }).catch(() => {});
        } catch (_) {}
        try { localStorage.removeItem('accessToken'); } catch {}
        try { localStorage.removeItem('refreshToken'); } catch {}
        location.replace('/login?' + encodeURIComponent(reason));
    }

    // ===== 이벤트 바인딩 =====
    if (btnVerify) {
        btnVerify.addEventListener('click', async (e) => {
            e.preventDefault();
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
                if (!res.ok) throw new Error('2차 인증 실패');

                const data = await res.json().catch(() => ({}));
                if (data && (data.ok || data.success)) {
                    location.replace(data.redirect || '/admin/');
                } else {
                    setMsg(data?.message || '인증 실패');
                }
            } catch (e2) {
                console.error(e2);
                setMsg(e2.message || '처리 중 오류가 발생했습니다.');
            }
        });
    }

    if (btnCancel) {
        btnCancel.addEventListener('click', (e) => {
            e.preventDefault();
            logoutSilent('mfaCanceled');
        });
    }

    // ===== 뒤로가기 방지(선택) =====
    (function preventBack() {
        const url = location.href;
        history.replaceState({ stay: true }, '', url);
        history.pushState({ stay: true }, '', url);
        window.addEventListener('popstate', (e) => {
            if (e.state && e.state.stay) {
                history.pushState({ stay: true }, '', url);
            }
        });
        window.addEventListener('pageshow', function (e) {
            if (e.persisted) {
                history.replaceState({ stay: true }, '', url);
                history.pushState({ stay: true }, '', url);
            }
        });
        document.addEventListener('keydown', (e) => {
            const t = e.target;
            const typing = t && (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA' || t.isContentEditable);
            if (!typing && e.key === 'Backspace') e.preventDefault();
        });
    })();
})();
