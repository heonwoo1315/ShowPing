package com.ssginc.showpingrefactoring.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class CategoryProductController {

    @Value("${portone.store-id}")
    private String storeId;

    @Value("${portone.channel-key}")
    private String channelKey;

    @GetMapping("/category/{categoryNo}")
    public String viewCategoryProducts(@PathVariable Long categoryNo, Model model) {
        model.addAttribute("categoryNo", categoryNo);
        return "product/product_list"; // category.html 렌더링
    }

    @GetMapping("/product/detail/{productNo}")
    public String viewProductDetail(@PathVariable Long productNo, Model model) {
        model.addAttribute("productNo", productNo);
        model.addAttribute("reviewNo", productNo);
        return "product/product_detail"; // templates/product/productDetail.html 반환
    }

//    @GetMapping("/cart")
//    public String viewCart(@PathVariable Long memberNo, Model model) {
//        model.addAttribute("memberNo", memberNo);
//        return "product/product_cart";
//    }

    @GetMapping("/cart")
    public String viewCart() {
        return "product/product_cart";
    }

    @GetMapping("/payment")
    public String viewPayment(Model model) {
        model.addAttribute("storeId", storeId);
        model.addAttribute("channelKey", channelKey);
        return "product/product_payment";
    }

    @GetMapping("/portone")
    public String viewPortone() {
        return "test_payment";
    }

    @GetMapping("/success")
    public String viewSucess() {
        return "payment/success";
    }
}