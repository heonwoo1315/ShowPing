package com.ssginc.showpingrefactoring.domain.payment.service.implement;

import com.ssginc.showpingrefactoring.domain.payment.service.PortOneService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Configuration
public class PortOneServiceImpl implements PortOneService {
    @Value("portone.api-url")
    private String PORTONE_API_URL;
    @Value("portone.secret-key")
    private String API_SECRET;
    @Value("portone.api-key")
    private String API_KEY;

    @Override
    public String getPortOneAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("imp_key", API_KEY);
        body.put("imp_secret", API_SECRET);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(PORTONE_API_URL + "/users/getToken", request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return (String) ((Map<String, Object>) response.getBody().get("response")).get("access_token");
        } else {
            throw new RuntimeException("PortOne 인증 토큰 발급 실패");
        }
    }
}