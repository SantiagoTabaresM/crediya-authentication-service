package co.com.pragma.api.config;

import co.com.pragma.model.utils.gateways.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerConfig {

    @Bean
    public Logger reactiveLogger() {
        return new co.com.pragma.api.config.ReactiveLoggerAdapter();
    }
}