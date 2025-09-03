package co.com.pragma.r2dbc;

import co.com.pragma.r2dbc.entity.UserEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface UserReactiveRepository extends ReactiveCrudRepository<UserEntity, Long>, ReactiveQueryByExampleExecutor<UserEntity> {

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByDocument(String document);


    Mono<Boolean> existsByDocumentAndEmail(String document, String email);

    @Query("SELECT u.document FROM users u WHERE u.user_id = :id")
    Mono<String> findDocumentoById(Long id);

    Mono<UserEntity> findByEmail(String email);

    Flux<UserEntity> findByDocumentIn(List<String> documents);

}
