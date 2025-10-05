package com.example.SWP.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.SWP.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.expiration-days:30}")
    private long expirationDays;


    public String generateAccessToken(User user) {
        Date now = Date.from(Instant.now());
        Date expiryDate = Date.from(Instant.now().plus(expirationDays, ChronoUnit.DAYS));
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

        return JWT.create()
                .withSubject(user.getEmail())
                .withIssuer(jwtIssuer)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("role", user.getRole())
                .sign(algorithm);
    }

    public DecodedJWT decode(String token) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        return JWT.require(algorithm).withIssuer(jwtIssuer).build().verify(token);
    }
}
