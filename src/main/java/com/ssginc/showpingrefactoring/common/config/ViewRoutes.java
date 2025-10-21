package com.ssginc.showpingrefactoring.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ViewRoutes implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // /stepup.html → templates/login/stepup.html
        registry.addViewController("/stepup.html").setViewName("login/stepup");
        // 원한다면 /login/stepup 도 열어둠
        registry.addViewController("/login/stepup").setViewName("login/stepup");
        // ViewRoutes.java
        registry.addViewController("/enroll_mobile.html").setViewName("login/enroll_mobile");

        registry.addRedirectViewController("/admin", "/admin/");   // /admin -> /admin/
        registry.addViewController("/admin/").setViewName("index"); // templates/index.html
    }
}
