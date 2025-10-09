package com.example.SWP.service.auth;

import com.example.SWP.dto.request.auth.GoogleLoginRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service

public class GoogleClientService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public GoogleIdToken.Payload verifyGoogleIdToken(GoogleLoginRequest googleLoginRequest){
        try{
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .setIssuer("https://accounts.google.com")
                    .build();
            GoogleIdToken idToken = verifier.verify(googleLoginRequest.getCredential());
            if(idToken != null){
                GoogleIdToken.Payload payload = idToken.getPayload();

                if(!payload.getEmailVerified()){
                    throw new RuntimeException("Email is not verified.");
                }

                if(!payload.getAudience().equals(clientId)){
                    throw new RuntimeException("Audience is not matched.");
                }

                return payload;
            }
            return null;
        }catch (Exception e){
            throw new RuntimeException("Failed to verify Google ID token.");
        }
    }

}
