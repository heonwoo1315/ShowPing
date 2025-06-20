document.addEventListener("DOMContentLoaded", function () {
    // 초기화
    document.getElementById("password").value = "";
    document.getElementById("confirm-password").value = "";
    document.getElementById("email").value = "";
});

// 비밀번호 토글 기능
document.querySelectorAll('.toggle-password').forEach(button => {
    button.addEventListener('click', function () {
        const input = this.previousElementSibling;
        if (input.type === "password") {
            input.type = "text";
            this.textContent = "🔒";
        } else {
            input.type = "password";
            this.textContent = "👁";
        }
    });
});

function redirectToHome() {
    window.location.href = "/";
}

// 이메일 인증 코드 확인 버튼 이벤트
document.querySelector('.verify-code-btn').addEventListener('click', function () {
    const email = document.getElementById('email').value.trim();
    const emailCode = document.getElementById('email-code').value.trim();

    console.log("인증 코드 확인 버튼 클릭됨!");
    console.log("보낼 데이터:", { email: email, emailCode: emailCode });

    axios.post('/api/member/verify-code', {
        email: email,
        emailCode: emailCode
    })
        .then(response => {
            console.log("서버 응답:", response.data);
            if (response.data === true) {
                Swal.fire({
                    icon: 'success',
                    title: '인증 성공',
                    text: '이메일 인증이 완료되었습니다.'
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: '인증 실패',
                    text: '이메일 인증 코드가 일치하지 않습니다.'
                });
            }
        })
        .catch(error => {
            console.error("오류 발생:", error);
            Swal.fire({
                icon: 'error',
                title: '오류 발생',
                text: '이메일 인증 중 문제가 발생했습니다.'
            });
        });
});

// 회원가입 버튼 이벤트
document.querySelector('.signup-btn').addEventListener('click', function (event) {
    event.preventDefault();

    const name = document.getElementById('name').value.trim();
    const email = document.getElementById('email').value.trim();
    const emailCode = document.getElementById('email-code').value.trim();
    const memberId = document.getElementById('memberId').value.trim();
    const password = document.getElementById('password').value.trim();
    const confirmPassword = document.getElementById('confirm-password').value.trim();
    const address = document.getElementById('address').value.trim();
    const phone = document.getElementById('phone').value.trim();

    // 유효성 검사
    if (!memberId) {
        Swal.fire('입력 오류', '아이디를 입력해주세요.', 'warning');
        return;
    }
    if (!validateMemberId(memberId)) {
        Swal.fire('형식 오류', '아이디는 영문 또는 숫자로 6~20자여야 합니다.', 'warning');
        return;
    }
    if (!password) {
        Swal.fire('입력 오류', '비밀번호를 입력해주세요.', 'warning');
        return;
    }
    if (!confirmPassword) {
        Swal.fire('입력 오류', '비밀번호를 확인해주세요.', 'warning');
        return;
    }
    if (!validatePassword(password) || !validatePassword(confirmPassword)) {
        Swal.fire('형식 오류', '비밀번호는 문자, 숫자, 특수문자를 포함해 8~20자여야 합니다.', 'warning');
        return;
    }
    if (!checkPasswordMatch()) {
        Swal.fire('불일치', '비밀번호가 일치하지 않습니다.', 'warning');
        return;
    }
    if (!name) {
        Swal.fire('입력 오류', '이름을 입력해주세요.', 'warning');
        return;
    }
    if (!phone) {
        Swal.fire('입력 오류', '핸드폰 번호를 입력해주세요.', 'warning');
        return;
    }
    if (!validatePhone(phone)) {
        Swal.fire('형식 오류', '올바른 핸드폰 번호를 입력해주세요.(010-1234-5678)', 'warning');
        return;
    }
    if (!email) {
        Swal.fire('입력 오류', '이메일을 입력해주세요.', 'warning');
        return;
    }
    if (!validateEmail(email)) {
        Swal.fire('형식 오류', '올바른 이메일 주소를 입력하세요.', 'warning');
        return;
    }
    if (!emailCode) {
        Swal.fire('입력 오류', '이메일 인증 코드를 입력해주세요.', 'warning');
        return;
    }
    if (!address) {
        Swal.fire('입력 오류', '주소를 입력해주세요.', 'warning');
        return;
    }

    // 중복 확인 순서: 아이디 -> 전화번호 -> 이메일 -> 인증 -> 가입
    fetch(`/api/member/check-duplicate?id=${memberId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('중복된 아이디입니다.');
            }
            return fetch(`/api/member/check-phone-duplicate?phone=${phone}`);
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('중복된 전화번호입니다.');
            }
            return fetch(`/api/member/check-email-duplicate?email=${email}`);
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('중복된 이메일입니다.');
            }
            return axios.post('/api/member/verify-code', { email: email, emailCode: emailCode });
        })
        .then(response => {
            if (!response.data) {
                Swal.fire({
                    icon: 'error',
                    title: '인증 실패',
                    text: '이메일 인증 코드가 일치하지 않습니다.'
                }).then(() => {
                    window.location.reload();
                });
                return;
            }

            return axios.post('/api/member/register', {
                memberId: memberId,
                memberPassword: password,
                memberName: name,
                memberPhone: phone,
                memberEmail: email,
                memberAddress: address,
            });
        })
        .then(response => {
            if (response) {
                Swal.fire({
                    icon: 'success',
                    title: '회원가입 완료',
                    text: '환영합니다! 로그인 페이지로 이동합니다.'
                }).then(() => {
                    window.location.href = "/login";
                });
            }
        })
        .catch(error => {
            console.error("오류 발생:", error);
            Swal.fire({
                icon: 'error',
                title: '오류 발생',
                text: error.message || '문제가 발생했습니다.'
            }).then(() => {
                window.location.reload();
            });
        });

});

// 이메일 인증 코드 전송 버튼 이벤트
function sendVerificationCode() {
    const email = document.getElementById('email').value.trim();

    if (!email) {
        Swal.fire('입력 오류', '이메일을 입력해주세요.', 'warning');
        return;
    }
    if (!validateEmail(email)) {
        Swal.fire('형식 오류', '올바른 이메일 주소를 입력하세요.', 'warning');
        return;
    }

    fetch('/api/member/send-code', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email })
    })
        .then(response => response.text())
        .then(data => {
            Swal.fire('전송 완료', '인증 코드가 이메일로 전송되었습니다.', 'success');
            console.log("서버 응답:", data);
            document.getElementById('email-verify-section').classList.remove('hidden');
        })
        .catch(error => {
            console.error("오류 발생:", error);
            Swal.fire('전송 실패', '이메일 전송에 실패했습니다.', 'error');
        });
}

// 유효성 검사 함수들
function validateMemberId(memberId) {
    const memberIdRegex = /^[A-Za-z0-9]{6,20}$/;
    return memberIdRegex.test(memberId);
}

function validatePassword(password) {
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>]).{8,20}$/;
    return passwordRegex.test(password);
}

function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function validatePhone(phone) {
    const phoneRegex = /^01[0-9]-\d{3,4}-\d{4}$/;
    return phoneRegex.test(phone);
}

function checkPasswordMatch() {
    const password = document.getElementById('password').value.trim();
    const confirmPassword = document.getElementById('confirm-password').value.trim();
    const messageDiv = document.getElementById('confirm-password-message');

    if (password === '' || confirmPassword === '') {
        messageDiv.style.display = 'none';
        return false;
    }

    if (password !== confirmPassword) {
        messageDiv.textContent = "비밀번호가 같지 않습니다.";
        messageDiv.style.color = 'red';
        messageDiv.style.display = 'block';
        return false;
    } else {
        messageDiv.textContent = "비밀번호가 같습니다.";
        messageDiv.style.color = 'green';
        messageDiv.style.display = 'block';
        return true;
    }
}

function checkDuplicate() {
    const memberId = document.getElementById("memberId").value.trim();
    const memberIdRegex = /^[A-Za-z0-9]{6,20}$/;

    if (!memberIdRegex.test(memberId)) {
        Swal.fire('형식 오류', '아이디는 영문 또는 숫자로 6~20자여야 합니다.', 'warning');
        return;
    }

    fetch(`/api/member/check-duplicate?id=${memberId}`)
        .then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('중복된 아이디입니다.');
            }
        })
        .then(message => {
            Swal.fire({
                icon: 'success',
                title: '사용 가능',
                text: '사용 가능한 아이디입니다.'
            });
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                title: '중복 아이디',
                text: error.message
            });
        });
}

// 아이디 입력 시 유효성 검사
document.getElementById('memberId').addEventListener('input', function () {
    const memberId = this.value.trim();
    const messageDiv = document.getElementById('memberId-message');

    if (memberId === '') {
        messageDiv.style.display = 'none';
        return;
    }

    if (!validateMemberId(memberId)) {
        messageDiv.textContent = "아이디는 영문 또는 숫자로 6~20자여야 합니다.";
        messageDiv.style.color = 'red';
        messageDiv.style.display = 'block';
    } else {
        messageDiv.textContent = "사용 가능한 아이디입니다.";
        messageDiv.style.color = 'green';
        messageDiv.style.display = 'block';
    }
});

// 비밀번호 입력 시 유효성 검사
document.getElementById('password').addEventListener('input', function () {
    const password = this.value.trim();
    const messageDiv = document.getElementById('password-message');

    if (password === '') {
        messageDiv.style.display = 'none';
        return;
    }

    if (!validatePassword(password)) {
        messageDiv.textContent = "비밀번호는 문자, 숫자, 특수문자를 포함해 8~20자여야 합니다.";
        messageDiv.style.color = 'red';
        messageDiv.style.display = 'block';
    } else {
        messageDiv.textContent = "사용 가능한 비밀번호입니다.";
        messageDiv.style.color = 'green';
        messageDiv.style.display = 'block';
    }
});

document.getElementById('password').addEventListener('input', checkPasswordMatch);
document.getElementById('confirm-password').addEventListener('input', checkPasswordMatch);
