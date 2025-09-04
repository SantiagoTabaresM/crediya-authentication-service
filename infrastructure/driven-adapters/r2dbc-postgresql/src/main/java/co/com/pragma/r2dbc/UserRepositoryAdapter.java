package co.com.pragma.r2dbc;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.r2dbc.entity.UserEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class UserRepositoryAdapter extends ReactiveAdapterOperations<
        User/* change for domain model */,
        UserEntity/* change for adapter model */,
        Long,
        UserReactiveRepository
> implements UserRepository {
    public UserRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, User.class));
    }

    @Override
    @Transactional
    public Mono<User> save(User user) {
        return super.save(user);
    }

    @Override
    @Transactional
    public Flux<User> findByDocument(List<String> document) {
        return repository.findByDocumentIn(document).map(this::toEntity);
    }


    @Override
    public Flux<User> findAll() {
        return super.findAll();
    }


    @Override
    @Transactional
    public Mono<User> findById(Long id) {
        return super.findById(id);
    }

    @Override
    @Transactional
    public Mono<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toEntity);
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id);
    }

    @Override
    @Transactional
    public Mono<Boolean> existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Mono<Boolean> existsByDocument(String document) {
        return repository.existsByDocument(document);
    }

    @Override
    @Transactional
    public Mono<Boolean> existsByDocumentAndEmail(String document, String email) {
        return repository.existsByDocumentAndEmail(document, email);
    }
}
