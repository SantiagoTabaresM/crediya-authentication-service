package co.com.pragma.model.utils.gateways;

import reactor.core.publisher.Mono;

public interface TokenProviderPort {
    Mono<String> generateToken(String userId, String role);

}
