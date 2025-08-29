package co.com.pragma.api.config;

import co.com.pragma.model.utils.gateways.TokenProviderPort;
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
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private final String secretKey;
    private Key key;

    private final long expirationMs;


    public JwtTokenProviderAdapter(@Value("${jwt.secret-key}") String secretKey,
                                   @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    @Override
    public Mono<String> generateToken(String username, String role) {
        return Mono.fromSupplier(() -> createToken(username, role));
    }


    public String createToken(String userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setIssuer("self") // Tu propio issuer
                .setAudience("myclientid") // Client ID si lo validas
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


}
