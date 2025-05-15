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
                                .requestMatchers("/**", "/css/**", "/js/**", "/img/**").permitAll()
                        // 공개 접근 가능한 URL (두 코드 블록의 permitAll 목록 통합)
//                        .requestMatchers(
//                                "/", "/login", "/webrtc/watch", "/webrtc/watch/**", "/css/**", "/js/**", "/images/**",
//                                "/img/**", "/assets/**", "/oauth/**", "/api/register", "/api/auth/login", "/api/auth/logout",
//                                "/api/auth/user-info", "/api/admin/login", "/product/detail/**", "/api/categories", "/category/**",
//                                "/api/products/**", "/stream/list", "/login/signup", "/check-duplicate", "/check-email-duplicate", "/check-duplicate/**", "/signup/send-code",
//                                "/signup/verify-code", "/check-email-duplicate/**", "/register", "/api/admin/verify-totp",
//                                "/api/admin/totp-setup/**", "/api/auth/refresh-token-check/**", "/stream/broadcast", "/stream/vod/list/page/**",
//                                "/watch/history", "/watch/vod/**", "/cart/**", "/api/carts/**", "/payment/**", "/api/orders/**", "/success/**",
//                                "/favicon.ico", "/api/auth/**", "/user/userInfo", "/webrtc/webrtc/", "/report/report/", "/api/member/check-duplicate",
//                                "api/member/check-email-duplicate"
//                        ).permitAll()
//                        // ADMIN 전용 URL (두 코드 블록의 ADMIN 관련 URL 병합)
//                        .requestMatchers("/admin/**", "/stream/stream")
//                        .hasRole("ADMIN")
//                        // USER 전용 URL (두 코드 블록의 USER 관련 URL 병합)
//                        .requestMatchers(
//                                "/user/**", "/api/payments/**", "/api/orders/**", "/watch/vod/**"
//                        ).hasAnyRole("USER", "ADMIN")
//                        // 그 외 모든 요청은 허용
//                        .anyRequest().authenticated()
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
}
