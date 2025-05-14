package com.ssginc.showpingrefactoring.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "ShowPingLive API",
                description = "ShowPingLive REST API Documentation",
                version = "1.0",
                contact = @Contact(name = "ShowPingLive Dev", url = "http://localhost:8080/", email = "SSGFinal@gmail.com"),
                license = @License(name = "ShowPingLive License", url = "http://localhost:8080/license") // 라이선스 상세 설명 페이지 (추후 추가)
        ),
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(url = "http://localhost:8080", description = "로컬 개발 서버"),
                @io.swagger.v3.oas.annotations.servers.Server(url = "https://showping.duckdns.org", description = "운영 서버")
        }
)
@Configuration
public class SwaggerConfig {
    // springdoc-openapi 자동 설정
}