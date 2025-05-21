package com.ssginc.showpingrefactoring.domain.payment.controller;

import com.ssginc.showpingrefactoring.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "결제 API", description = "결제 관련 API입니다.")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "결제 검증", description = "impUid를 사용해 결제를 검증합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 검증 성공"),
        @ApiResponse(responseCode = "400", description = "impUid 누락 또는 잘못된 요청", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody Map<String, String> request) {
        String impUid = request.get("impUid");
        if (impUid == null) {
            return ResponseEntity.badRequest().body("impUid가 없습니다.");
        }

        String result = paymentService.verifyPayment(impUid);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "결제 완료 처리", description = "결제 완료 시 호출되며 상태를 응답합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 완료 처리 성공"),
        @ApiResponse(responseCode = "400", description = "결제 ID가 없거나 유효하지 않음", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/complete")
    public ResponseEntity<?> completePayment(@RequestBody Map<String, Object> requestData) {
        String paymentId = (String) requestData.get("paymentId");

        if (paymentId == null || paymentId.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid payment ID.");
        }

        // 결제 완료 처리 (예: 데이터베이스 저장 등)
        System.out.println("결제 완료: " + paymentId);

        // 결제 상태 응답
        Map<String, String> response = new HashMap<>();
        response.put("status", "PAID");
        return ResponseEntity.ok(response);
    }
}
