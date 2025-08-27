package co.com.pragma.api.config;

import co.com.pragma.model.utils.gateways.Logger;
import org.slf4j.LoggerFactory;

public class ReactiveLoggerAdapter implements Logger {
    // usamos el logger de SLF4J explícitamente
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(ReactiveLoggerAdapter.class);

    @Override
    public void info(String message) {
        logger.info("[Reactive] " + message);
    }


    @Override
    public void error(String message, Throwable throwable) {
        logger.error("[Reactive] " + message, throwable);
    }

    @Override
    public void debug(String message) {
        logger.debug("[Reactive] " +  message);
    }
}
