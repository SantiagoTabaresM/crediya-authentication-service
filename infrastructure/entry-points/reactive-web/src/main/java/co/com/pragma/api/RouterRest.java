package co.com.pragma.api;

import co.com.pragma.api.config.UserPath;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final UserPath userPath;
    private final UserHandler userHandler;

    @Bean
    public RouterFunction<ServerResponse> routerFunction(UserHandler handler) {
        return route(POST(userPath.getUsers()), userHandler::listenSaveUser)
                .andRoute(PUT(userPath.getUsers()), userHandler::listenUpdateUser)
                .andRoute(DELETE(userPath.getUsersById()), userHandler::listenDeleteUser)
                .andRoute(GET(userPath.getUsers()), userHandler::listenGetAllUsers)
                .andRoute(GET(userPath.getUsersById()), userHandler::listenGetUserById);
    }
}
