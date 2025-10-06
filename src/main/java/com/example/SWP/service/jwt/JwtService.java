package com.example.SWP.service.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.SWP.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service

public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    public String generateAccessToken(User user) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withIssuer(issuer)
                .withClaim("role", user.getRole().toString())
                .withClaim("provider", user.getProvider().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessExpiration))
                .sign(Algorithm.HMAC256(secretKey));
    }

    public DecodedJWT decode(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.require(algorithm).withIssuer(issuer).build().verify(token);
    }
}
