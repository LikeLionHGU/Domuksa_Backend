package org.example.emmm.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleIdTokenVerifierService {

    @Value("${google.client-id}")
    private String googleClientId;

    public GoogleIdToken.Payload verifyAndGetPayload(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(
                            GoogleNetHttpTransport.newTrustedTransport(),
                            GsonFactory.getDefaultInstance())
                            .setAudience(Collections.singletonList(googleClientId))
                            .build();

            GoogleIdToken token = verifier.verify(idTokenString);
            if (token == null) {
                throw new IllegalArgumentException("Invalid Google ID Token");
            }
            return token.getPayload();
        } catch (Exception e) {
            throw new IllegalArgumentException("Google ID Token verification failed", e);
        }
    }
}
