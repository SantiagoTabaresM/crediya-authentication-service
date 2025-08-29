package co.com.pragma.usecase.auth;

import co.com.pragma.model.auth.Auth;
import co.com.pragma.model.role.gateways.RoleRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.model.utils.gateways.Logger;
import co.com.pragma.model.utils.gateways.PasswordEncoderPort;
import co.com.pragma.model.utils.gateways.TokenProviderPort;
import co.com.pragma.model.utils.gateways.TxOperational;
import co.com.pragma.usecase.user.exception.BussinesException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class AuthUseCase implements IAuthUseCase{

    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_PASSWORD = "password";

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final String ERROR_VALIDATION = "Validation errors";


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final Logger logger;
    private final TxOperational txOperational;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;

    @Override
    public Mono<User> validateUser(User user) {
        return txOperational.execute(() -> {
            logger.info("Attempting to login user: " + user.getEmail());
            return validate(user, this::validateLogin)
                    .flatMap(this::validatePassword);
        });

    }


    private Map<String, String> validateLogin(User auth) {
        Map<String, String> errors = createErrorMap();

        // Validar password
        validateNotBlank(auth.getPassword(), FIELD_PASSWORD,
                "password is required", errors);

        // Validar email
        validateEmail(auth.getEmail(), FIELD_EMAIL, errors);

        return errors;
    }

    @Override
    public Mono<Auth> generateToken(User user) {
        return getRoleName(user) // obtener rol y userId
                .flatMap(auth ->
                        tokenProvider.generateToken(auth.getUserId().toString(), auth.getRole()) // generar JWT
                                .map(token -> auth.toBuilder()
                                        .token(token) // asignar token
                                        .build()
                                )
                );
    }

    private Mono<Auth> getRoleName(User user) {
        return roleRepository.findById(user.getRoleId())
                .map(role ->
                    Auth.builder()
                            .userId(user.getId())
                            .role(role.getName()) // asignar el nombre del rol
                            .token(null) // token null por ahora
                            .build()
                )
                .switchIfEmpty(Mono.defer(() -> {
                    logger.error("Role not found with email: " + user.getRoleId(), null);
                    Map<String, String> errors = createErrorMap();
                    errors.put("Role", "Role not found with email: " + user.getRoleId());
                    return Mono.error(new BussinesException(ERROR_VALIDATION, errors));
                }));
    }






    private Mono<User> validatePassword(User user) {
        return userRepository.findByEmail(user.getEmail())
                .flatMap(userDb ->
                        passwordEncoder.matches(user.getPassword(), userDb.getPassword())
                                .flatMap(isValid -> {
                                    if (Boolean.TRUE.equals(isValid)) {
                                        return Mono.just(userDb);
                                    } else {
                                        logger.error("The password is incorrect to " + user.getEmail(), null);
                                        Map<String, String> errors = createErrorMap();
                                        errors.put(FIELD_PASSWORD, "The password is incorrect");
                                        return Mono.error(new BussinesException(ERROR_VALIDATION, errors));
                                    }
                                })
                )
                .switchIfEmpty(Mono.defer(() -> {
                    logger.error("User not found with email: " + user.getEmail(), null);
                    Map<String, String> errors = createErrorMap();
                    errors.put(FIELD_EMAIL, "User not found with email: " + user.getEmail());
                    return Mono.error(new BussinesException(ERROR_VALIDATION, errors));
                }));
    }




    /**
     * Valida un objeto y retorna un Mono con el objeto si es válido
     */
    public <T> Mono<T> validate(T object, Function<T, Map<String, String>> validationFunction) {
        return Mono.defer(() -> {
            Map<String, String> errors = validationFunction.apply(object);

            if (!errors.isEmpty()) {
                return Mono.error(new BussinesException("Validation errors in fields", errors));
            }

            return Mono.just(object);
        });
    }


    /**
     * Valida que un campo no sea nulo o vacío
     */
    public void validateNotBlank(String value, String fieldName, String errorMessage, Map<String, String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.put(fieldName, errorMessage);
        }
    }

    /**
     * Valida el formato de email
     */
    public void validateEmail(String email, String fieldName, Map<String, String> errors) {
        if (email == null || email.trim().isEmpty()) {
            errors.put(fieldName, "Email is required");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.put(fieldName, "The email format is invalid");
        }
    }


    /**
     * Helper method para crear mapas de errores
     */
    public static Map<String, String> createErrorMap() {
        return new LinkedHashMap<>();
    }





}
