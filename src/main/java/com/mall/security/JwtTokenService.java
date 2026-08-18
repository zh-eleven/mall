package com.mall.security;

import com.mall.common.enums.PrincipalType;
import com.mall.admin.entity.UmsAdmin;
import com.mall.member.entity.UmsMember;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UmsMember member) {
        return generateToken(
                member.getUsername(),
                member.getId(),
                PrincipalType.MEMBER
        );
    }

    public String generateToken(UmsAdmin admin) {
        return generateToken(
                admin.getUsername(),
                admin.getId(),
                PrincipalType.ADMIN
        );
    }

    private String generateToken(
            String username,
            Long userId,
            PrincipalType principalType) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim(
                        "principalType",
                        principalType.name()
                )
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plusSeconds(expiration)
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        Number userId = getClaims(token)
                .get("userId", Number.class);

        return userId.longValue();
    }

    public PrincipalType getPrincipalType(String token) {
        String value = getClaims(token)
                .get("principalType", String.class);

        return PrincipalType.valueOf(value);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);

            String principalType =
                    claims.get(
                            "principalType",
                            String.class
                    );

            return PrincipalType.MEMBER.name()
                    .equals(principalType)
                    || PrincipalType.ADMIN.name()
                    .equals(principalType);

        } catch (Exception exception) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}