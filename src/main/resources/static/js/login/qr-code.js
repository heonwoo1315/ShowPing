async function fetchQrCode() {
    const adminId = sessionStorage.getItem("memberId");

    if (!adminId) {
        alert("로그인 정보가 없습니다. 다시 로그인해주세요.");
        window.location.href = "/login";
        return;
    }

    try {
        const response = await axios.get(`/api/admin/2fa/setup`, {
            params: {
                memberId: adminId
            }
        });

        // 서버에서 바로 QR코드 URL 문자열로 응답
        document.getElementById("qrCodeImage").src =
            `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent(response.data)}`;

    } catch (error) {
        console.error("QR 코드 로드 오류:", error);
        alert("QR 코드 로드 중 오류 발생!");
    }
}

window.onload = fetchQrCode;