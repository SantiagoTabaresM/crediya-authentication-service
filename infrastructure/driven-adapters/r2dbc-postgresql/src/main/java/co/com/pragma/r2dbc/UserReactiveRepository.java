package co.com.pragma.r2dbc;

import co.com.pragma.r2dbc.entity.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


public interface UserReactiveRepository extends ReactiveCrudRepository<UserEntity, Long>, ReactiveQueryByExampleExecutor<UserEntity> {
    @Transactional
    Mono<Boolean> existsByEmail(String email);

    @Transactional
    Mono<Boolean> existsByDocumentAndEmail(String document, String email);

    Mono<UserEntity> findByEmail(String email);

}
