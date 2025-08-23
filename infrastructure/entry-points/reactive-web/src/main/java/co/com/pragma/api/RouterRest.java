package co.com.pragma.api;

import co.com.pragma.api.config.UserPath;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.logging.Handler;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final UserPath userPath;
    private final HandlerV1 userHandler;

    @Bean
    public RouterFunction<ServerResponse> routerFunction(HandlerV1 handler) {
        return route(POST("/users"), userHandler::listenSaveUser);
    }
}
