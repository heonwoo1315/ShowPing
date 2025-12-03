'use strict';

// --------- 유틸 ----------
function getCookie(name){
    return document.cookie.split('; ').find(r=>r.startsWith(name+'='))?.split('=')[1];
}

function extractTotpSecret(otpauth) {
    if (!otpauth) return '';
    const q = otpauth.indexOf('?');
    if (q < 0) return '';
    const params = new URLSearchParams(otpauth.slice(q + 1));
    return params.get('secret') || '';
}

async function ensureCsrfCookie() {
    try { await fetch('/api/csrf', { credentials: 'include' }); } catch (_) {}
}
function authzHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    const at = (window.getAccessToken && window.getAccessToken()) || localStorage.getItem('accessToken');
    if (at) headers['Authorization'] = 'Bearer ' + at;
    const xsrf = getCookie('XSRF-TOKEN');
    if (xsrf) headers['X-XSRF-TOKEN'] = decodeURIComponent(xsrf);
    return headers;
}
function qs(name, def='') {
    const u = new URL(location.href);
    return u.searchParams.get(name) ?? def;
}
function setMsg(t, ok=false){
    const el = document.getElementById('msg');
    el.style.color = ok?'#065f46':'#b91c1c';
    el.textContent = t||'';
}
function b64uToBytes(b64url) {
    const pad = '='.repeat((4 - b64url.length % 4) % 4);
    const base64 = (b64url.replace(/-/g, '+').replace(/_/g, '/')) + pad;
    const bin = atob(base64);
    const buf = new Uint8Array(bin.length);
    for (let i=0;i<bin.length;i++) buf[i] = bin.charCodeAt(i);
    return buf.buffer;
}
function bytesToB64u(buf) {
    const bytes = new Uint8Array(buf);
    let bin = '';
    for (let i=0;i<bytes.length;i++) bin += String.fromCharCode(bytes[i]);
    return btoa(bin).replace(/\+/g,'-').replace(/\//g,'_').replace(/=+$/,'');
}
function goStepup() { location.replace('/stepup.html'); } // ← 검증창으로 통일 이동

// --------- 서버 통신 ----------
async function creationOptions(inviteId){
    const res = await fetch('/auth/mfa/enroll/options?inviteId='+encodeURIComponent(inviteId), {
        method:'GET', headers:authzHeaders(), credentials:'same-origin'
    });
    if (!res.ok) throw new Error('옵션 조회 실패: '+res.status);
    return res.json();
}
async function doCreate(pubKeyOpts){
    if (!('credentials' in navigator) || !('create' in navigator.credentials)) {
        throw new Error('이 브라우저는 WebAuthn(등록)을 지원하지 않습니다.');
    }
    const publicKey = {
        challenge: b64uToBytes(pubKeyOpts.challenge),
        rp: pubKeyOpts.rp,
        user: {
            id: b64uToBytes(pubKeyOpts.user.id),
            name: pubKeyOpts.user.name,
            displayName: pubKeyOpts.user.displayName
        },
        pubKeyCredParams: pubKeyOpts.pubKeyCredParams,
        authenticatorSelection: pubKeyOpts.authenticatorSelection || { authenticatorAttachment:'platform', residentKey:'preferred', userVerification:'preferred' },
        attestation: pubKeyOpts.attestation || 'none'
    };
    const cred = await navigator.credentials.create({ publicKey });
    return {
        rawId: bytesToB64u(cred.rawId),
        attestationObject: bytesToB64u(cred.response.attestationObject),
        clientDataJSON: bytesToB64u(cred.response.clientDataJSON)
    };
}
async function sendAttest(inviteId, payload){
    const res = await fetch('/auth/mfa/enroll/attest', {
        method:'POST', headers:authzHeaders(), credentials:'same-origin',
        body: JSON.stringify({ inviteId, ...payload })
    });
    if (!res.ok) throw new Error('등록 실패: '+res.status+' '+ await res.text().catch(()=>'' ));
    return res.json(); // { deviceId, otpauth }
}
async function verifyTotp(inviteId, code){
    const res = await fetch('/auth/mfa/enroll/verify', {
        method:'POST', headers:authzHeaders(), credentials:'same-origin',
        body: JSON.stringify({ inviteId, totp: code })
    });
    if (!res.ok) throw new Error('TOTP 검증 실패');
    return res.json();
}

// ---------- UI 바인딩 ----------
window.addEventListener('DOMContentLoaded', () => {
    const btnStart  = document.getElementById('btnStart');
    const btnCancel = document.getElementById('btnCancel');
    const btnVerify = document.getElementById('btnVerify');
    const totp      = document.getElementById('totp');
    const otpauth   = document.getElementById('otpauth');

    const inviteId = qs('inviteId');

    //  요구사항: 취소와 동일하게 stepup으로
    if (btnCancel) {
        btnCancel.addEventListener('click', () => goStepup());
    }

    //  등록 시작: (1) 정상 WebAuthn 등록 시 기존 동작 유지
    //              (2) 초대가 없거나/만료/오류면 곧바로 stepup으로 우회
    if (btnStart) {
        btnStart.addEventListener('click', async () => {
            setMsg('');
            btnStart.disabled = true;
            try{
                await ensureCsrfCookie();

                // 초대가 없으면 즉시 우회
                if (!inviteId) { goStepup(); return; }

                // 1) 옵션 조회 → 2) WebAuthn 등록 → 3) 서버 저장
                const opts    = await creationOptions(inviteId);
                const payload = await doCreate(opts);
                const resp    = await sendAttest(inviteId, payload);

                // 안내 + TOTP 입력란 노출(원래 플로우 유지)
                const secret = extractTotpSecret(resp.otpauth);
                otpauth.textContent = '등록 키값: ' + (secret || '(서버 미제공)');
                document.getElementById('verifyRow').style.display = 'flex';
                setMsg('단말 등록 완료. Google Authenticator에 계정 추가 후, 6자리 코드를 입력하세요.', true);
            }catch(e){
                // 🔁 초대 만료/서버오류는 즉시 검증창으로 우회
                const m = String(e && e.message || '');
                if (m.includes('Invite') || m.includes('옵션 조회 실패') || /\b(404|409|410|500)\b/.test(m)) {
                    goStepup(); return;
                }
                console.error(e);
                setMsg(m || '등록에 실패했습니다.');
            }finally{
                btnStart.disabled = false;
            }
        });
    }

    //  TOTP 검증: 성공하면 자동으로 stepup.html 이동
    if (btnVerify) {
        btnVerify.addEventListener('click', async () => {
            setMsg('');
            const code = (totp.value||'').trim();
            if(!/^\d{6}$/.test(code)){ setMsg('TOTP 6자리를 입력하세요.'); return; }
            try{
                if (!inviteId) { goStepup(); return; }
                await ensureCsrfCookie();
                await verifyTotp(inviteId, code);
                setMsg('TOTP 검증 완료. 2차 인증 화면으로 이동합니다…', true);
                setTimeout(goStepup, 600);
            }catch(e){
                console.error(e);
                setMsg(e.message || 'TOTP 검증 실패');
            }
        });
    }
});
