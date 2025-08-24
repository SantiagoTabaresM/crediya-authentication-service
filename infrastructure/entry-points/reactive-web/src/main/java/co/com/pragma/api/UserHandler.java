package co.com.pragma.api;

import co.com.pragma.api.dto.CreateUserDTO;
import co.com.pragma.api.dto.UpdateUserDTO;
import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.api.mapper.UserDTOMapper;
import co.com.pragma.api.validation.UserValidator;
import co.com.pragma.usecase.user.UserUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
@Tag(name = "User API", description = "Reactive User Management")
public class UserHandler {

    private final UserUseCase userUseCase;

    private final UserValidator userValidator;
    private final UserDTOMapper userDTOMapper;

    public Mono<ServerResponse> listenSaveUser(ServerRequest serverRequest) {
        Mono<CreateUserDTO> userMono = serverRequest.bodyToMono(CreateUserDTO.class);

        return userMono
                .flatMap(userValidator::validateCreateUser)
                .map(userDTOMapper::toUser)
                .flatMap(userUseCase::saveUser)
                .map(userDTOMapper::toUserDTO)
                .flatMap(savedUser -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedUser));
    }

    public Mono<ServerResponse> listenUpdateUser(ServerRequest serverRequest) {
        Mono<UpdateUserDTO> userMono = serverRequest.bodyToMono(UpdateUserDTO.class);
        return userMono
                .map(userDTOMapper::updateUserDTOtoUser)
                .flatMap(userUseCase::updateUser)
                .map(userDTOMapper::toUserDTO)
                .flatMap(savedUser -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedUser));
    }


    public Mono<ServerResponse> listenGetAllUsers(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                //.contentType(MediaType.APPLICATION_NDJSON)
                //.contentType(MediaType.TEXT_EVENT_STREAM)
                .body(
                        userUseCase.getAllUsers()
                                .map(userDTOMapper::toUserDTO), // convertir cada user a DTO
                        UserDTO.class
                );
    }

    public Mono<ServerResponse> listenGetUserById(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");

        return userUseCase.getUserById(id)
                .map(userDTOMapper::toUserDTO) // convertir a DTO
                .flatMap(userDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userDTO))
                .switchIfEmpty(ServerResponse.notFound().build());
    }


    public Mono<ServerResponse> listenDeleteUser(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");

        return userUseCase.deleteUser(id)
                .then(ServerResponse.noContent().build());
    }


}
