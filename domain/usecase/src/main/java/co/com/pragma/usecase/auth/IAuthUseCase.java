package co.com.pragma.usecase.auth;

import co.com.pragma.model.auth.Auth;
import co.com.pragma.model.user.User;
import reactor.core.publisher.Mono;

public interface IAuthUseCase {
    public Mono<User> validateUser(User user);

    public  Mono<Auth> getRoleInfo(User user);

}
