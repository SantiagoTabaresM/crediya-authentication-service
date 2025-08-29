package co.com.pragma.api.config;

import co.com.pragma.model.utils.gateways.PasswordEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Component
public class BCryptPasswordEncoderPortAdapter implements PasswordEncoderPort {

    private final PasswordEncoder delegate;

    public BCryptPasswordEncoderPortAdapter(PasswordEncoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<String> encode(String rawPassword) {
        // BCrypt es CPU-bound. Ejecutamos en boundedElastic para no bloquear Netty.
        return Mono.fromCallable(() -> delegate.encode(rawPassword))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> matches(String rawPassword, String encodedPassword) {
        return Mono.fromCallable(() -> delegate.matches(rawPassword, encodedPassword))
                .subscribeOn(Schedulers.boundedElastic());
    }
}