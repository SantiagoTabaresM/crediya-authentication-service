package co.com.pragma.api;

import co.com.pragma.api.dto.CreateUserDTO;
import co.com.pragma.api.mapper.UserDTOMapper;
import co.com.pragma.model.user.User;
import co.com.pragma.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class HandlerV1 {

    private final UserUseCase userUseCase;

    private final UserDTOMapper userDTOMapper;

    public Mono<ServerResponse> listenSaveUser(ServerRequest serverRequest) {
        Mono<CreateUserDTO> userMono = serverRequest.bodyToMono(CreateUserDTO.class);

        return userMono
                .map(userDTOMapper::toUser)
                .flatMap(userUseCase::saveUser)
                .flatMap(savedUser -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedUser))
                .onErrorResume(e -> {
                    // Manejo genérico
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "error", e.getMessage(),
                                    "timestamp", System.currentTimeMillis()
                            ));
                });
    }


}
