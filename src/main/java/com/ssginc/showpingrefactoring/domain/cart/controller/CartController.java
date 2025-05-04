package com.ssginc.showpingrefactoring.domain.cart.controller;


import com.ssginc.showpingrefactoring.domain.member.dto.object.MemberDto;
import com.ssginc.showpingrefactoring.domain.cart.dto.request.CartRequestDto;
import com.ssginc.showpingrefactoring.domain.cart.dto.object.CartDto;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.cart.service.implement.CartServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartServiceImpl cartService;
    private final MemberRepository memberRepository;

    //특정 회원의 장바구니 조회
    @GetMapping("/{memberNo}")
    public ResponseEntity<List<CartDto>> getCartByMemberNo(@PathVariable Long memberNo) {
        List<CartDto> cartList = cartService.getCartByMemberNo(memberNo);
        return ResponseEntity.ok(cartList);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestParam Long memberNo, @RequestBody CartRequestDto requestDto) {
        cartService.addToCart(memberNo, requestDto);
        return ResponseEntity.ok("상품이 장바구니에 추가되었습니다.");
    }

    //장바구니 상품 수량 수정
    @PutMapping("/update")
    public ResponseEntity<String> updateCartItem(@RequestParam Long memberNo, @RequestBody CartRequestDto requestDto) {
        cartService.updateCartItem(memberNo, requestDto);
        return ResponseEntity.ok("장바구니 상품 수량이 수정되었습니다.");
    }

    //장바구니 상품 삭제
    @DeleteMapping("/remove")
    public ResponseEntity<String> removeCartItem(@RequestParam Long memberNo, @RequestParam Long productNo) {
        cartService.removeCartItem(memberNo, productNo);
        return ResponseEntity.ok("장바구니에서 상품이 삭제되었습니다.");
    }

    @GetMapping("/info")
    public ResponseEntity<?> getMemberInfo(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String username = userDetails.getUsername();
        Member member = memberRepository.findByMemberId(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // Member 엔티티에서 필요한 데이터만 추출하여 MemberDTO로 변환
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

