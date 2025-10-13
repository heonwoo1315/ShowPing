document.addEventListener('DOMContentLoaded', () => {
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
                await Swal.fire({
                    icon: 'success',
                    title: '관리자 로그인 성공',
                    text: '관리자 권한으로 로그인되었습니다.',
                    confirmButtonText: '확인'
                });
            } else {
                await Swal.fire({
                    icon: 'success',
                    title: '로그인 성공',
                    text: '정상적으로 로그인되었습니다.',
                    confirmButtonText: '확인'
                });
            }

            // 4) 홈으로 이동 (필요시 원하는 경로로 바꿔도 됨)
            window.location.replace('/');

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
포인트