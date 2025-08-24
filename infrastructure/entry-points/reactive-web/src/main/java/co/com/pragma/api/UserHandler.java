package co.com.pragma.api;

import co.com.pragma.api.dto.CreateUserDTO;
import co.com.pragma.api.dto.UpdateUserDTO;
import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.api.exception.ValidationException;
import co.com.pragma.api.mapper.UserDTOMapper;
import co.com.pragma.api.validation.ReactiveValidator;
import co.com.pragma.api.validation.UserValidator;
import co.com.pragma.usecase.user.UserUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
@Tag(name = "User API", description = "Reactive User Management")
public class UserHandler {

    private final UserUseCase userUseCase;

    private final UserValidator userValidator;
    private final UserDTOMapper userDTOMapper;

    public Mono<ServerResponse> listenSaveUser(ServerRequest serverRequest) {
        log.info("Received request to create new user");
        Mono<CreateUserDTO> userMono = serverRequest.bodyToMono(CreateUserDTO.class);
        return userMono
                .flatMap(userValidator::validateCreateUser)
                .flatMap(dto -> userUseCase.existsByEmail(dto.email())
                        .flatMap(exists -> {
                            if (Boolean.TRUE.equals(exists)) {
                                Map<String, String> errors = ReactiveValidator.createErrorMap();
                                errors.put("email", "The email address is already registered by another user");
                                return Mono.error(new ValidationException("Validation errors", errors));
                            }
                            return Mono.just(dto);
                        })
                )
                .map(userDTOMapper::toUser)
                .flatMap(userUseCase::saveUser)
                .map(userDTOMapper::toUserDTO)
                .flatMap(savedUser -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedUser))
                .doOnError(e -> log.error("Error creating new user", e))
                .doOnSuccess(resp -> log.info("User created successfully"));
    }

    public Mono<ServerResponse> listenUpdateUser(ServerRequest serverRequest) {
        Mono<UpdateUserDTO> userMono = serverRequest.bodyToMono(UpdateUserDTO.class);
        return userMono
                .map(userDTOMapper::updateUserDTOtoUser)
                .flatMap(userUseCase::updateUser)
                .map(userDTOMapper::toUserDTO)
                .flatMap(savedUser -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedUser))
                .doOnError(e -> log.error("Error updating new user", e))
                .doOnSuccess(resp -> log.info("User updated successfully"));
    }


    public Mono<ServerResponse> listenGetAllUsers(ServerRequest serverRequest) {
        log.info("Received request to get all users");
        return  userUseCase.getAllUsers()
                .map(userDTOMapper::toUserDTO)
                .collectList()
                .flatMap(usersList -> {
                    log.info("Returning {} users", usersList.size());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(usersList);
                })
                .doOnError(e -> log.error("Error fetching users", e))
                .doOnSuccess(resp -> log.info("Successfully returned all users"));
    }

    public Mono<ServerResponse> listenGetUserById(ServerRequest serverRequest) {
        log.info("Received parameter to find user by id");
        Long id = Long.valueOf(serverRequest.pathVariable("id"));
        return userUseCase.getUserById(id)
                .map(userDTOMapper::toUserDTO) // convertir a DTO
                .flatMap(userDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userDTO))
                .switchIfEmpty(ServerResponse.notFound().build())
                .doOnError(e -> log.error("Error finding user", e))
                .doOnSuccess(resp -> log.info("User finding successfully"));
    }


    public Mono<ServerResponse> listenDeleteUser(ServerRequest serverRequest) {
        log.info("Received parameter to delete user by id");
        Long id = Long.valueOf(serverRequest.pathVariable("id"));
        return userUseCase.deleteUser(id)
                .then(ServerResponse.noContent().build())
                .doOnError(e -> log.error("Error deleting new user", e))
                .doOnSuccess(resp -> log.info("User deleted successfully"));
    }


}
