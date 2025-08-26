package co.com.pragma.usecase.user;

import co.com.pragma.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// PUERTOS PRIMARIOS

public interface IUserUseCase {

    public Mono<User> saveUser(User user);

    public Mono<User> updateUser(User user);

    public Flux<User> getAllUsers();

    public Mono<User> getUserById(Long id);

    public Mono<Void> deleteUser(Long id) ;


}
