package com.ssginc.showpingrefactoring.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class Page403Controller {
    @GetMapping("/error-page/403")
    public String error403() {
        return "error/403"; // templates/error/403.html
    }
}
