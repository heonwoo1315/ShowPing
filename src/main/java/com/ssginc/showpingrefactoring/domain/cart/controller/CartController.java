package com.ssginc.showpingrefactoring.domain.cart.controller;

import com.ssginc.showpingrefactoring.domain.member.dto.object.MemberDto;
import com.ssginc.showpingrefactoring.domain.cart.dto.request.CartRequestDto;
import com.ssginc.showpingrefactoring.domain.cart.dto.object.CartDto;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.cart.service.implement.CartServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "장바구니 API", description = "장바구니 관련 API입니다.")
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartServiceImpl cartService;
    private final MemberRepository memberRepository;

    @Operation(summary = "회원 장바구니 조회", description = "회원 번호를 이용해 장바구니 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "장바구니 조회 성공")
    @GetMapping("/{memberNo}")
    public ResponseEntity<List<CartDto>> getCartByMemberNo(
            @Parameter(description = "회원 번호", required = true)
            @PathVariable Long memberNo) {
        List<CartDto> cartList = cartService.getCartByMemberNo(memberNo);
        return ResponseEntity.ok(cartList);
    }

    @Operation(summary = "장바구니에 상품 추가", description = "회원 번호와 상품 정보를 통해 장바구니에 상품을 추가합니다.")
    @ApiResponse(responseCode = "200", description = "장바구니 추가 성공")
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @Parameter(description = "회원 번호", required = true)
            @RequestParam Long memberNo,
            @RequestBody CartRequestDto requestDto) {
        cartService.addToCart(memberNo, requestDto);
        return ResponseEntity.ok("상품이 장바구니에 추가되었습니다.");
    }

    @Operation(summary = "장바구니 수량 수정", description = "회원 번호와 상품 정보를 통해 장바구니 수량을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "장바구니 수정 성공")
    @PutMapping("/update")
    public ResponseEntity<String> updateCartItem(
            @Parameter(description = "회원 번호", required = true)
            @RequestParam Long memberNo,
            @RequestBody CartRequestDto requestDto) {
        cartService.updateCartItem(memberNo, requestDto);
        return ResponseEntity.ok("장바구니 상품 수량이 수정되었습니다.");
    }

    @Operation(summary = "장바구니 상품 삭제", description = "회원 번호와 상품 번호를 통해 장바구니에서 상품을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "장바구니 삭제 성공")
    @DeleteMapping("/remove")
    public ResponseEntity<String> removeCartItem(
            @Parameter(description = "회원 번호", required = true)
            @RequestParam Long memberNo,
            @Parameter(description = "상품 번호", required = true)
            @RequestParam Long productNo) {
        cartService.removeCartItem(memberNo, productNo);
        return ResponseEntity.ok("장바구니에서 상품이 삭제되었습니다.");
    }

    @Operation(summary = "회원 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공")
    @GetMapping("/info")
    public ResponseEntity<?> getMemberInfo(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String username = userDetails.getUsername();
        Member member = memberRepository.findByMemberId(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        MemberDto memberDTO = new MemberDto();
        memberDTO.setMemberNo(member.getMemberNo());
        memberDTO.setMemberId(member.getMemberId());
        memberDTO.setMemberName(member.getMemberName());
        memberDTO.setMemberEmail(member.getMemberEmail());
        memberDTO.setMemberAddress(member.getMemberAddress());
        memberDTO.setMemberPhone(member.getMemberPhone());

        return ResponseEntity.ok(memberDTO);
    }
}
