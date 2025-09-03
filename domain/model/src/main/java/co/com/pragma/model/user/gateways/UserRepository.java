package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

// PUERTOS SECUNDARIOS
public interface UserRepository {

    Mono<User> save(User user);

    Flux<User> findAll();

    Mono<User> findById(Long id);

    Mono<Void> deleteById(Long id);

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByDocument(String document);

    Mono<Boolean> existsByDocumentAndEmail(String document, String email);

    Mono<User> findByEmail(String email);

    Flux<User> findByDocument(List<String> document);

}
