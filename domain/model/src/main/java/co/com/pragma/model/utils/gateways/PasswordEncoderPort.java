package co.com.pragma.model.utils.gateways;

import reactor.core.publisher.Mono;

public interface PasswordEncoderPort {
    Mono<String> encode(String rawPassword);
    Mono<Boolean> matches(String rawPassword, String encodedPassword);
}
