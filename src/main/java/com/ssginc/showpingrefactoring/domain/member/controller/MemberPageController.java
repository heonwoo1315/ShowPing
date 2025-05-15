package com.ssginc.showpingrefactoring.domain.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPageController {
    @GetMapping("/login")
    public String loginPage() {
        return "login/login";
    }

    @GetMapping("/login/signup")
    public String signupPage() {
        return "login/signup";
    }
}
