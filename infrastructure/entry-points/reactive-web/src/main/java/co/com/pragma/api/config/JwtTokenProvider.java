package co.com.pragma.api.config;

import co.com.pragma.api.dto.AuthDTO;
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

@Component
public class JwtTokenProvider {

    private Key key;

    private final long expirationMs;


    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey,
                            @Value("${jwt.expiration-ms}") long expirationMs) {

        this.expirationMs = expirationMs;
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }



    public Mono<String> generateToken(String username, String role) {
        return Mono.fromSupplier(() -> createToken(username, role));
    }

    public Mono<AuthDTO> generateToken(AuthDTO auth) {
        return generateToken(auth.document(), auth.role()) // devuelve Mono<String> con el token
                .map(token -> new AuthDTO(auth.document(), token, auth.role()));
    }

    public String createToken(String document, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                //.setIssuer("self") // Tu propio issuer
                //.setAudience("myclientid") // Client ID si lo validas
                .setClaims(claims)
                .setSubject(document)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


}
