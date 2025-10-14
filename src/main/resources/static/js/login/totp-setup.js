window.onload = async function() {
    const adminId = "ShowPing_Admin"; // 관리자는 로그인 후 동적으로 가져오는 것이 더 안전

    try {
        // ✅ 변경된 API 경로와 호출 방식 (Query Param 사용)
        const response = await axios.get(`/api/admin/2fa/setup`, {
            params: {
                memberId: adminId
            }
        });

        // ✅ 서버가 otpauth://... 문자열 그대로 반환하므로 바로 표시
        document.getElementById("secretKey").textContent = response.data;

    } catch (error) {
        console.error("TOTP 설정 요청 실패:", error);
        alert("TOTP 설정 중 오류가 발생했습니다.");
    }
};