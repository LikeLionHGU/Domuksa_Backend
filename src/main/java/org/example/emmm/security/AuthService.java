package org.example.emmm.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ExternalProperties externalProperties;

    public Algorithm getTokenAlgorithm() {
        return Algorithm.HMAC512(externalProperties.getSecretKey());
    }

    public String createAccessToken(Long userId) {
        return JWT.create()
                .withSubject("accessToken")
                .withClaim("id", userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + externalProperties.getAccessExpirationTime()))
                .sign(getTokenAlgorithm());
    }

    public Long verifyAccessToken(String accessToken) throws JWTVerificationException {
        return JWT.require(getTokenAlgorithm())
                .build()
                .verify(accessToken)
                .getClaim("id").asLong();
    }
}
