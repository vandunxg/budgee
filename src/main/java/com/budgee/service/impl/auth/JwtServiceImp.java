package com.budgee.service.impl.auth;

import static com.budgee.enums.TokenType.ACCESS_TOKEN;
import static com.budgee.enums.TokenType.REFRESH_TOKEN;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.budgee.enums.TokenType;
import com.budgee.exception.AuthenticationException;
import com.budgee.exception.ErrorCode;
import com.budgee.model.User;
import com.budgee.service.JwtService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "JWT-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtServiceImp implements JwtService {

    // -------------------------------------------------------------------
    // PRIVATE VALUE
    // -------------------------------------------------------------------

    @NonFinal
    @Value("${jwt.private-key.access-token}")
    String ACCESS_TOKEN_PRIVATE_KEY;

    @NonFinal
    @Value("${jwt.private-key.refresh-token}")
    String REFRESH_TOKEN_PRIVATE_KEY;

    @NonFinal
    @Value("${jwt.expiration.access-token}")
    String ACCESS_TOKEN_EXPIRY_TIME;

    @NonFinal
    @Value("${jwt.expiration.refresh-token}")
    String REFRESH_TOKEN_EXPIRY_TIME;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    public String generateAccessToken(User user) {
        log.info(
                "generate access token for user {} with authorities {}",
                user.getEmail(),
                user.getAuthorities());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        log.info("email={} userId={}", user.getEmail(), user.getId());
        return generateAccessToken(claims, user.getEmail());
    }

    @Override
    public String generateRefreshToken(User user) {
        log.info(
                "generate refresh token for user {} with authorities {}",
                user.getEmail(),
                user.getAuthorities());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put(
                "role",
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        return generateRefreshToken(claims, user.getEmail());
    }

    @Override
    public String extractEmail(String token, TokenType type) {
        log.info("extractEmail");

        return extractClaim(token, type, Claims::getSubject);
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    String generateAccessToken(Map<String, Object> claims, String email) {
        log.info("----------[ GENERATE-ACCESS-TOKEN ]----------");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + Long.parseLong(ACCESS_TOKEN_EXPIRY_TIME)))
                .signWith(getKeys(ACCESS_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    String generateRefreshToken(Map<String, Object> claims, String email) {
        log.info("----------[ GENERATE-REFRESH-TOKEN ]----------");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + Long.parseLong(REFRESH_TOKEN_EXPIRY_TIME)))
                .signWith(getKeys(REFRESH_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    Key getKeys(TokenType type) {
        log.info("----------[ GET-KEY ]----------");

        switch (type) {
            case ACCESS_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(ACCESS_TOKEN_PRIVATE_KEY));
            }

            case REFRESH_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(REFRESH_TOKEN_PRIVATE_KEY));
            }

            default -> throw new AuthenticationException(ErrorCode.INVALID_TOKEN_TYPE);
        }
    }

    <T> T extractClaim(String token, TokenType type, Function<Claims, T> claimResolver) {
        log.info("----------[ extractClaim ]----------");

        final Claims claims = extraAllClaim(token, type);
        return claimResolver.apply(claims);
    }

    Claims extraAllClaim(String token, TokenType type) {
        log.info("----------[ extraAllClaim ]----------");

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getKeys(type))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) { // Invalid signature or expired token
            throw new AccessDeniedException("Access denied: " + e.getMessage());
        }
    }
}
