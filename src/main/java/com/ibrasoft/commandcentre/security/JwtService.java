package com.ibrasoft.commandcentre.security;

import com.ibrasoft.commandcentre.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String ROLES_CLAIM = "roles";

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.issuer:command-centre}")
    private String jwtIssuer;

    @Value("${security.jwt.user-ttl-seconds:604800}")
    private long userTtlSeconds;

    @Value("${security.jwt.bot-ttl-seconds:31536000}")
    private long botTtlSeconds;

    public String generateUserToken(String subject, Collection<Role> roles) {
        return generateToken(subject, roles, userTtlSeconds);
    }

    public String generateBotToken(String subject, Collection<Role> roles) {
        return generateToken(subject, roles, botTtlSeconds);
    }

    public JwtClaims parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .requireIssuer(jwtIssuer)
            .build()
            .parseClaimsJws(token)
            .getBody();

        List<String> roles = claims.get(ROLES_CLAIM, List.class);
        if (roles == null) {
            roles = List.of();
        }
        return new JwtClaims(claims.getSubject(), roles);
    }

    private String generateToken(String subject, Collection<Role> roles, long ttlSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
            .setIssuer(jwtIssuer)
            .setSubject(subject)
            .claim(ROLES_CLAIM, roles.stream().map(Role::name).toList())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
