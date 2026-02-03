package org.example.emmm.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GoogleTokenExchangeService {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public String exchangeCodeForIdToken(String code) {
        String url = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);

        ResponseEntity<Map> res = restTemplate.postForEntity(url, req, Map.class);

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalArgumentException("Google token exchange failed");
        }

        Object idToken = res.getBody().get("id_token");
        if (idToken == null) {
            throw new IllegalArgumentException("Google did not return id_token");
        }

        return idToken.toString();
    }
}
