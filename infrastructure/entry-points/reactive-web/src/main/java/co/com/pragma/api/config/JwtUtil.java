package co.com.pragma.api.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class JwtUtil {

    private final String secretKey;
    private Key key;

    private final long expirationMs;


    public JwtUtil(@Value("${jwt.secret-key}") String secretKey,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public Mono<String> generateTokenReactive(String username, Set<String> roles) {
        return Mono.fromSupplier(() -> generateToken(username, roles));
    }

    public String generateToken(String username, Set<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
