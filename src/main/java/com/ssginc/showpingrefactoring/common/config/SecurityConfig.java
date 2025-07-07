package com.ssginc.showpingrefactoring.common.config;

import com.ssginc.showpingrefactoring.common.exception.CustomAccessDeniedHandler;
import com.ssginc.showpingrefactoring.common.exception.CustomAuthenticationEntryPoint;
import com.ssginc.showpingrefactoring.common.jwt.JwtFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정: 프로덕션과 로컬 둘 다 허용
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("https://showping.duckdns.org", "http://localhost:8080"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                // CSRF 비활성화 및 세션 관리 stateless 설정 (JWT 방식)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 엔드포인트 별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
//                                .requestMatchers("/**", "/css/**", "/js/**", "/img/**").permitAll()
                                // 공개 접근 가능한 URL (두 코드 블록의 permitAll 목록 통합)
                                .requestMatchers(
                                        "/", "/login", "/webrtc/watch", "/webrtc/watch/**", "/css/**", "/js/**", "/images/**",
                                        "/img/**", "/assets/**", "/oauth/**", "/api/register", "/api/auth/login", "/api/auth/logout",
                                        "/api/auth/user-info", "/api/admin/login", "/product/detail/**", "/api/categories/**", "/category/**",
                                        "/api/products/**", "/api/admin/verify-totp", "/login/signup/**", "/api/member/verify-code",
                                        "/api/admin/totp-setup/**", "/api/auth/refresh-token-check/**", "/stream/broadcast", "/stream/vod/list/page/**",
                                        "/favicon.ico", "/api/auth/**",  "/api/member/check-duplicate", "/api/member/register",
                                        "/api/member/send-code/**", "/api/member/check-email-duplicate", "/api/member/check-phone-duplicate", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html","/api/batch/**", "/api/live/standby"
                                        ,"/api/live/product/list", "/api/live/onair", "/api/live/live-info", "/api/live/active", "/stream/watch/**", "/stream/list/**", "/watch/**",
                                        "/api/watch/insert","/product/product_list","/product/product_list/**","/product/product_detail/**","/record", "/live"
                                ).permitAll()
                                // ADMIN 전용 URL (두 코드 블록의 ADMIN 관련 URL 병합)
                                .requestMatchers("/admin/**","/api/live/stop", "/api/live/start", "/api/live/register", "/api/report/updateStatus", "/api/report/register",
                                        "/api/report/report", "/api/chatRoom/create", "/api/vod/upload", "/api/void/subtitle/**", "/stream/stream", "/report/**", "/api/report/list")
                                .hasRole("ADMIN")
                                // USER 전용 URL (두 코드 블록의 USER 관련 URL 병합)
                                .requestMatchers(
                                        "/user/**","/api/carts/**", "/api/payments/**", "/api/orders/**", "/api/watch/history/**", "/api/hls/**", "/api/payments/verify",
                                        "/api/payments/complete", "/api/vod/list/**", "/api/chat/**", "/watch/history/**", "/cart/**", "/product/product_cart", "/payment/**",
                                        "/product/product_payment", "/success/**", "/payment/success/**"
                                ).hasAnyRole("USER", "ADMIN")
                                .anyRequest().authenticated()
                )
                // 로그인, 로그아웃 기능 비활성화 (JWT 사용)
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())

                // 예외 처리 핸들러 추가
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        log.info("securityConfig 적용 완료");
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Thymeleaf에서 Security 기능 사용 가능하도록 설정
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    // 로그인 성공 시 처리 (메인페이지로 리다이렉트)
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> response.sendRedirect("/");
    }

    // 로그아웃 성공 시 처리 (로그아웃 후 로그인 페이지로 이동)
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> response.sendRedirect("/login");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:8080", "https://showping.duckdns.org") // 프론트엔드 도메인들
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true); // 중요
            }
        };
    }
}
