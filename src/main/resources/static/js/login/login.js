document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(location.search || '');
    if (params.has('mfaCanceled') || params.has('enrollCanceled')) {
        try {
            if (window.ensureCsrfCookie) await window.ensureCsrfCookie();
            if (window.authApi)        await window.authApi.post('logout');
        } catch (e) {
            console.warn('auto-logout on login page failed (ignored):', e);
        } finally {
            try { localStorage.removeItem('accessToken'); } catch {}
            try { localStorage.removeItem('refreshToken'); } catch {}
            // 주소창에서 쿼리 제거(새로고침해도 반복 로그아웃 방지)
            history.replaceState(null, '', location.pathname);
        }
    }

    const form = document.getElementById('loginForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const memberId =
            document.getElementById('memberId')?.value?.trim();
        const password =
            document.getElementById('password')?.value?.trim() ??
            document.getElementById('memberPassword')?.value?.trim();

        if (!memberId || !password) {
            return Swal.fire({
                icon: 'warning',
                title: '입력 필요',
                text: '아이디와 비밀번호를 모두 입력하세요.',
                confirmButtonText: '확인'
            });
        }

        // 버튼 중복 클릭 방지
        const submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.dataset.originalText = submitBtn.textContent;
            submitBtn.textContent = '로그인 중...';
        }

        try {
            await window.ensureCsrfCookie?.();

            // 1) 로그인 (쿠키에 AT/RT 설정)
            await window.authApi.post('login', { memberId, password });

            // 2) 곧바로 내 정보 재조회해서 role 판단 (_skipRefresh: true로 게스트/401 재발급 방지)
            let roleUpper = '';
            try {
                const meRes = await window.authApi.get('user-info', { _skipRefresh: true });
                const me = meRes?.data || {};

                // 다양한 형태 방어적으로 대응: role / memberRole / roles[]
                const roleRaw =
                    me.role ??
                    me.memberRole ??
                    (Array.isArray(me.roles) && me.roles.find(r => /ADMIN/i.test(String(r)))) ??
                    '';
                roleUpper = String(roleRaw).toUpperCase();
            } catch (ignored) {
                // 조회 실패시엔 일반 사용자로 취급
                roleUpper = '';
            }

            // 3) SweetAlert2: 관리자/일반 분기
            if (roleUpper === 'ADMIN' || roleUpper === 'ROLE_ADMIN') {
                const ret = encodeURIComponent(location.pathname + location.search);
                location.href = `/stepup.html?return=${ret}`;
                return;
            } else {
                await Swal.fire({
                    icon: 'success',
                    title: '로그인 성공',
                    text: '정상적으로 로그인되었습니다.',
                    confirmButtonText: '확인'
                });
                window.location.replace('/');
            }

        } catch (err) {
            console.error(err);
            Swal.fire({
                icon: 'error',
                title: '로그인 실패',
                text: '아이디 또는 비밀번호를 확인하세요.',
                confirmButtonText: '확인'
            });
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = submitBtn.dataset.originalText || '로그인';
            }
        }
    });
});