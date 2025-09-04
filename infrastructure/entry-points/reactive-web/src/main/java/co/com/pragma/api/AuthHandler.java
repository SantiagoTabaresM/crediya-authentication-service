package co.com.pragma.api;

import co.com.pragma.api.config.JwtTokenProvider;
import co.com.pragma.api.dto.LoginDTO;
import co.com.pragma.api.mapper.AuthDTOMapper;
import co.com.pragma.api.mapper.UserDTOMapper;
import co.com.pragma.usecase.auth.IAuthUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
@Tag(name = "User API", description = "Reactive User Management")
public class AuthHandler {

    private final UserDTOMapper loginDTOMapper;
    private final AuthDTOMapper authDTOMapper;
    private final JwtTokenProvider jwt;

    private final IAuthUseCase authUseCase;



    public Mono<ServerResponse> listenLogin(ServerRequest serverRequest) {
        log.info("Received request to create new user");
        Mono<LoginDTO> loginDTOMono = serverRequest.bodyToMono(LoginDTO.class);
        return loginDTOMono
                .map(loginDTOMapper::toUser)
                .flatMap(authUseCase::validateUser)
                .flatMap(authUseCase::getRoleInfo)
                .map(authDTOMapper::toAuthDTO)
                .flatMap(jwt::generateToken)
                .flatMap(auth -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(auth))
                .doOnError(e -> log.error("Error login user", e))
                .doOnSuccess(resp -> log.info("JWT created successfully"));
    }


}
