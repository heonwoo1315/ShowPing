package com.ssginc.showpingrefactoring.domain.member.controller;

import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import com.ssginc.showpingrefactoring.domain.member.dto.object.MemberDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.EmailVerifyRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.SignupRequestDto;
import com.ssginc.showpingrefactoring.domain.member.dto.request.UpdateMemberRequestDto;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.service.MailService;
import com.ssginc.showpingrefactoring.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final Map<String, Boolean> verifiedEmailStorage = new HashMap<>();
    private final MailService mailService;

    /**
     * 회원 가입
     */
//    @PostMapping("/signup")
//    public ResponseEntity<Void> signup(@RequestBody SignupRequestDto request) {
//        memberService.signup(request);
//        return ResponseEntity.ok().build();
//    }

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 회원 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공")
    @GetMapping("/me")
    public ResponseEntity<MemberDto> getMyInfo(HttpServletRequest request) {
        String memberId = getMemberIdFromRequest(request);
        MemberDto memberDto = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(memberDto);
    }

    @Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공")
    @PutMapping("/me")
    public ResponseEntity<Void> updateMyInfo(@RequestBody UpdateMemberRequestDto request, HttpServletRequest httpRequest) {
        String memberId = getMemberIdFromRequest(httpRequest);
        memberService.updateMember(memberId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 계정을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(HttpServletRequest request) {
        String memberId = getMemberIdFromRequest(request);
        memberService.deleteMember(memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * AccessToken에서 memberId 추출 (Authorization 헤더 사용)
     */
    private String getMemberIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // 여기는 JwtTokenProvider를 직접 주입하거나, 서비스에서 따로 빼야 해
            // 여기서는 MemberController에 JwtTokenProvider 추가 주입한다고 가정할게
            return jwtTokenProvider.getUsername(token);
        }
        throw new RuntimeException("No Authorization Header Found");
    }

    @Operation(summary = "이메일 인증 코드 전송", description = "입력한 이메일로 인증 코드를 전송합니다.")
    @PostMapping("/send-code")
    public String sendCode(@RequestBody EmailVerifyRequestDto mailDto) {
        return mailService.sendVerificationCode(mailDto.getEmail());
    }

    @Operation(summary = "이메일 인증 코드 검증", description = "입력한 인증 코드가 유효한지 확인합니다.")
    @PostMapping("/verify-code")
    public boolean verifyCode(@RequestBody EmailVerifyRequestDto mailDto) {
        return mailService.verifyCode(mailDto.getEmail(), mailDto.getEmailCode());
    }

    @PostMapping("/register")
    public String registerMember(@RequestBody MemberDto memberDto, RedirectAttributes redirectAttributes) throws Exception {
        System.out.println(memberDto.toString());
        try {
            // 회원가입 처리 (회원 정보 DB 저장)
            Member member = memberService.registerMember(memberDto);
            // 성공 시 메시지와 함께 로그인 페이지로 리다이렉트
            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다.");
            return "redirect:/login";
        } catch (Exception e) {
            // 회원가입 실패 시 예외 처리
            redirectAttributes.addFlashAttribute("message", "회원가입에 실패했습니다. 다시 시도해주세요.");
            return "redirect:/login/signup";  // 실패 시 회원가입 페이지로 리다이렉트
        }
    }

    @Operation(summary = "ID 중복 확인", description = "입력한 ID가 중복되었는지 확인합니다.")
    @GetMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicate(@RequestParam("id") String memberId) {
        // ID 중복 확인 로직을 추가
        boolean isDuplicate = memberService.isDuplicateId(memberId);

        // 중복 여부에 따라 응답 처리
        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("중복된 아이디입니다");
        } else {
            return ResponseEntity.ok("사용 가능한 아이디입니다.");
        }
    }

    @Operation(summary = "이메일 중복 확인", description = "입력한 이메일이 중복되었는지 확인합니다.")
    @GetMapping("/check-email-duplicate")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam("email") String memberEmail) {
        // 이메일 중복 확인 로직을 추가
        boolean isDuplicate = memberService.isDuplicateEmail(memberEmail);

        // 중복 여부에 따라 응답 처리
        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("중복된 이메일입니다");
        } else {
            return ResponseEntity.ok("사용 가능한 이메일입니다.");
        }
    }

}
