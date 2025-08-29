package co.com.pragma.api.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Log4j2
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class AuthorizationJwt implements WebFluxConfigurer {

    private final String secretKey;
    private final String jsonExpRoles;

    private final ObjectMapper mapper;
    private static final String ROLE = "ROLE_";
    private static final String AZP = "azp";


    public AuthorizationJwt(@Value("${jwt.secret-key}") String secretKey,
                            @Value("${jwt.json-exp-roles}") String jsonExpRoles,
                            ObjectMapper mapper) {
        this.secretKey = secretKey;
        this.jsonExpRoles = jsonExpRoles;
        this.mapper = mapper;
    }

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorize -> authorize
                    .pathMatchers("/api/v1/auth/login").permitAll() // Permitir acceso sin autenticación
                    .pathMatchers("webjars/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyExchange().authenticated() // Todas las demás rutas requieren autenticación
            )
            .oauth2ResourceServer(oauth2 ->
                    oauth2.jwt(jwtSpec ->
                            jwtSpec
                            .jwtDecoder(jwtDecoder())
                            .jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                    )
            );
        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // Convertir secretKey a SecretKey
        SecretKey key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withSecretKey(key).build();

        // Validadores opcionales
        jwtDecoder.setJwtValidator(JwtValidators.createDefault());

        return jwtDecoder;
    }


    public Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        var jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt ->
                getRoles(jwt.getClaims(), jsonExpRoles)
                .stream()
                 .map(ROLE::concat)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
        return new ReactiveJwtAuthenticationConverterAdapter(jwtConverter);
    }

    private List<String> getRoles(Map<String, Object> claims, String jsonExpClaim) {
        try {
            var json = mapper.writeValueAsString(claims);
            var node = mapper.readTree(json).at(jsonExpClaim);

            if (node.isMissingNode() || node.isNull()) {
                return List.of(); // nunca null
            }

            if (node.isArray()) {
                return mapper.convertValue(node, new TypeReference<List<String>>() {});
            } else if (node.isTextual()) {
                return List.of(node.asText()); // si es string único
            } else {
                return List.of();
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            return List.of();
        }
    }
}
