package co.com.pragma.api;

import co.com.pragma.api.dto.CreateUserDTO;
import co.com.pragma.api.dto.UpdateUserDTO;

import co.com.pragma.api.mapper.UserDTOMapper;

import co.com.pragma.usecase.user.IUserUseCase;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


import java.util.Collections;

@Log4j2
@Component
@RequiredArgsConstructor
@Tag(name = "User API", description = "Reactive User Management")
public class UserHandler {

    private final IUserUseCase userUseCase;
    private final UserDTOMapper userDTOMapper;

    public Mono<ServerResponse> listenSaveUser(ServerRequest serverRequest) {
        log.info("Received request to create new user");
        Mono<CreateUserDTO> userMono = serverRequest.bodyToMono(CreateUserDTO.class);
        return userMono
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
                .doOnNext(model-> log.info("Fetched UserDTO : {}", model.id()))
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

    public Mono<ServerResponse> listenCheckUserExists(ServerRequest serverRequest) {
        log.info("Received request to check if user exists by name and email");

            // Extraer parámetros de la ruta
        String document = serverRequest.pathVariable("document");
        String email = serverRequest.pathVariable("email");

        return userUseCase.existsByDocumentAndEmail(document, email)
                .flatMap(exists -> {
                    if (exists) {
                        log.info("User with document [{}] and email [{}] exists", document, email);
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(true);
                    } else {
                        log.info("User with document [{}] and email [{}] does not exist", document, email);
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(false);
                    }
                })
                .doOnError(e -> log.error("Error validating user existence", e));
    }


}
