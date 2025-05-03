document.addEventListener("DOMContentLoaded", function () {
    const accessToken = sessionStorage.getItem('accessToken');
    const updateBtn = document.querySelector(".update-btn");    // 업데이트 버튼

    updateBtn.addEventListener("click", function () {
        // streamNo는 전역으로 선언된 streamNo 사용

        // streamTitle : 사용자가 입력한 방송 제목
        const streamTitle = document.getElementById("broadcastTitle").value;

        // streamDescription : 사용자가 입력한 방송 설명
        const streamDescription = document.getElementById("broadcastDesc").value;

        // productNo : class가 product-info인 div 태그의 id 값
        const productDiv = document.querySelector(".product-element .product-info");
        const productNo = productDiv ? productDiv.id : null;

        // productSale : id가 discountRate인 input 태그의 value 값
        const productSale = parseInt(document.getElementById("discountRate").value, 10);

        if (!streamTitle || !productNo) {
            Swal.fire({
                icon: 'info',
                title: "입력 오류",
                text: "방송 제목 입력과 상품 선택은 필수입니다."
            });
            return;
        }

        if (productSale > 100 || productSale < 0) {
            Swal.fire({
                icon: 'info',
                title: "입력 오류",
                text: "할인율은 100% 이하로 입력해주세요."
            });
            return;
        }

        const data = {
            streamNo: streamNo,
            streamTitle: streamTitle,
            streamDescription: streamDescription,
            productNo: productNo,
            productSale: productSale
        };

        const header = {
            "Authorization": "Bearer " + accessToken
        }

        // axios로 방송을 등록
        axios.post("/api/live/register",
            data, {
            headers: header
            })
            .then((response) => {
                Swal.fire({
                    icon: 'success',
                    title: "방송 정보 업데이트 완료",
                    text: "업데이트가 완료되었습니다."
                });
                
                streamNo = response.data.streamNo;

                $('#start').attr('disabled', false);
                $('#stop').attr('disabled', false);
            })
            .catch((error) => {
                console.log("실패", error);
            });
    });
});