package co.com.pragma.usecase.user;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.usecase.user.exception.BussinesException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;


@RequiredArgsConstructor
public class UserUseCase implements IUserUseCase {

    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_LAST_NAME = "last_name";
    private static final String FIELD_BASE_SALARY = "base_salary";
    private static final String FIELD_ID = "id";

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    
    private final UserRepository userRepository;

    public Mono<User> saveUser(User user) {
        return validate(user, this::validateCreateUser) // Primero validaciones normales
                .flatMap(validatedUser ->
                        userRepository.existsByEmail(validatedUser.getEmail()) // Luego validar unicidad de email
                                .flatMap(exists -> {
                                    if (Boolean.TRUE.equals(exists)) {
                                        Map<String, String> errors = createErrorMap();
                                        errors.put(FIELD_EMAIL, "The email address is already registered by another user");
                                        return Mono.error(new BussinesException("Validation errors", errors));
                                    }
                                    return userRepository.save(validatedUser);
                                })
                );
    }

    public Mono<User> updateUser(User user) {

        return validate(user, this::validateUpdateUser) // Primero validaciones normales
                .flatMap(validatedUser ->
                        userRepository.existsByEmail(validatedUser.getEmail()) // Luego validar unicidad de email
                                .flatMap(exists -> {
                                    if (Boolean.TRUE.equals(exists)) {
                                        Map<String, String> errors = createErrorMap();
                                        errors.put(FIELD_EMAIL, "The email address is already registered by another user");
                                        return Mono.error(new BussinesException("Validation errors", errors));
                                    }
                                    return userRepository.save(validatedUser);
                                })
                );
    }

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Mono<Void> deleteUser(Long id) {
        return userRepository.deleteById(id);
    }




    private Map<String, String> validateCreateUser(User user) {
        Map<String, String> errors = createErrorMap();
        // Validar nombres
        validateNotBlank(user.getName(), FIELD_NAME,
                "Name is required", errors);
        validateLength(user.getName(), FIELD_NAME, 2, 50, errors);

        // Validar apellidos
        validateNotBlank(user.getLastName(), FIELD_LAST_NAME,
                "Last name is required", errors);
        validateLength(user.getLastName(), FIELD_LAST_NAME, 2, 50, errors);

        // Validar email
        validateEmail(user.getEmail(), FIELD_EMAIL, errors);

        // Validar salario
        validateRange(user.getBaseSalary(), FIELD_BASE_SALARY, 0, 15000000,
                "Base salary must be greater than 0 and less than 15,000,000", errors);

        return errors;
    }

    private Map<String, String> validateUpdateUser(User user) {
        return validateCreateUser(user);
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
     * Valida que un número esté dentro de un rango [min, max]
     */
    public void validateRange(Number value, String fieldName, double min, double max, String errorMessage, Map<String, String> errors) {
        if (value == null) {
            errors.put(fieldName, "This field is required");
        } else if (value.doubleValue() < min || value.doubleValue() > max) {
            errors.put(fieldName, errorMessage != null ? errorMessage :
                    String.format("The value must be between %.2f and %.2f", min, max));
        }
    }


    /**
     * Valida la longitud de un string
     */
    public void validateLength(String value, String fieldName, int min, int max, Map<String, String> errors) {
        if (value != null) {
            int length = value.trim().length();
            if (length < min || length > max) {
                errors.put(fieldName, String.format("Must be between %d and %d characters", min, max));
            }
        }
    }

    /**
     * Helper method para crear mapas de errores
     */
    public static Map<String, String> createErrorMap() {
        return new LinkedHashMap<>();
    }
    
    


    
}
