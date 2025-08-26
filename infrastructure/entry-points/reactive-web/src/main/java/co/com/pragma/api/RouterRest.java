package co.com.pragma.api;


import co.com.pragma.api.dto.CreateUserDTO;
import co.com.pragma.api.dto.UpdateUserDTO;
import co.com.pragma.api.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final UserHandler userHandler;

    private static final String USERS = "/api/v1/users";
    private static final String USERS_BY_ID =  "/api/v1/users/{id}";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = USERS,
                    method = {RequestMethod.POST},
                    beanClass = UserHandler.class,
                    beanMethod = "listenSaveUser",
                    operation = @Operation(
                            operationId = "saveUser",
                            summary = "Create a new user",
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    required = true,
                                    description = "User data",
                                    content = @Content(schema = @Schema(implementation = CreateUserDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "User created successfully",
                                            content = @Content(schema = @Schema(implementation = UserDTO.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error")
                            }
                    )
            ),
            @RouterOperation(
                    path = USERS_BY_ID,
                    method = {RequestMethod.GET},
                    beanClass = UserHandler.class,
                    beanMethod = "listenGetUserById",
                    operation = @Operation(
                            operationId = "getUserById",
                            summary = "Get a user by ID",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "User ID")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "User found",
                                            content = @Content(schema = @Schema(implementation = UserDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "User not found")
                            }
                    )
            ),
            @RouterOperation(
                    path = USERS,
                    method = {RequestMethod.PUT},
                    beanClass = UserHandler.class,
                    beanMethod = "listenUpdateUser",
                    operation = @Operation(
                            operationId = "updateUser",
                            summary = "Update an existing user",
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    required = true,
                                    description = "Updated user data",
                                    content = @Content(schema = @Schema(implementation = UpdateUserDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "User updated successfully",
                                            content = @Content(schema = @Schema(implementation = UserDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "User not found")
                            }
                    )
            ),
            @RouterOperation(
                    path = USERS,
                    method = {RequestMethod.GET},
                    beanClass = UserHandler.class,
                    beanMethod = "listenGetAllUsers",
                    operation = @Operation(
                            operationId = "getAllUsers",
                            summary = "Get all users",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "List of users",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))
                            }
                    )
            ),
            @RouterOperation(
                    path = USERS_BY_ID,
                    method = {RequestMethod.DELETE},
                    beanClass = UserHandler.class,
                    beanMethod = "listenDeleteUser",
                    operation = @Operation(
                            operationId = "deleteUser",
                            summary = "Delete a user by ID",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "User ID")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                                    @ApiResponse(responseCode = "404", description = "User not found")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(UserHandler handler) {
        return route(POST(USERS), userHandler::listenSaveUser)
                .andRoute(PUT(USERS), userHandler::listenUpdateUser)
                .andRoute(DELETE(USERS_BY_ID), userHandler::listenDeleteUser)
                .andRoute(GET(USERS), userHandler::listenGetAllUsers)
                .andRoute(GET(USERS_BY_ID), userHandler::listenGetUserById);
    }
}
