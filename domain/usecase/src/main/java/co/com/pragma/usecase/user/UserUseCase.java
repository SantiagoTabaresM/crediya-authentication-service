package co.com.pragma.usecase.user;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.model.utils.gateways.Logger;
import co.com.pragma.model.utils.gateways.PasswordEncoderPort;
import co.com.pragma.model.utils.gateways.SecurityUtilsPort;
import co.com.pragma.model.utils.gateways.TxOperational;
import co.com.pragma.usecase.user.exception.BussinesException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Clase `UserUseCase` que implementa la interfaz `IUserUseCase`.
 * Esta clase contiene la lógica de negocio relacionada con los usuarios.
 * Utiliza inyección de dependencias para acceder al repositorio de usuarios,
 * el logger y las operaciones transaccionales.
 */
@RequiredArgsConstructor
public class UserUseCase implements IUserUseCase {


    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_LAST_NAME = "last_name";
    private static final String FIELD_BASE_SALARY = "base_salary";
    private static final String ERROR_VALIDATION = "Validation errors";

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    
    private final UserRepository userRepository;
    private final Logger logger;
    private final TxOperational txOperational;
    private final PasswordEncoderPort passwordEncoder;
    private final SecurityUtilsPort securityUtils;



    /**
     * Guarda un usuario en la base de datos.
     *
     * @param user El usuario a guardar.
     * @return Un `Mono` que emite el usuario guardado o un error si ocurre algún problema.
     */
    public Mono<User> saveUser(User user) {
        return txOperational.execute(() -> {
            logger.info("Attempting to save user with email: " + user.getEmail());
            return validate(user, this::validateCreateUser)
                    .flatMap(this::checkIfEmailExists)
                    .flatMap(this::validateRoleToCreate)
                    .flatMap(this::encryptPassword)
                    .map(u -> {
                        u.setRoleId(3);
                        return u;
                    })
                    .flatMap(userRepository::save)
                    .doOnSuccess(saved -> logger.info("User saved successfully with id: " + saved.getId()));
        });
    }

    /**
     * Verifica si el correo ya existe en la BD.
     */
    private Mono<User> checkIfEmailExists(User user) {
        return userRepository.existsByEmail(user.getEmail())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        logger.error("Email already exists: " + user.getEmail(), null);
                        Map<String, String> errors = createErrorMap();
                        errors.put(FIELD_EMAIL, "The email address is already registered by another user");
                        return Mono.error(new BussinesException(ERROR_VALIDATION, errors));
                    }
                    return Mono.just(user);
                });
    }

    private Mono<User> validateRoleToCreate(User user) {
        return securityUtils.getUserRole()
                .flatMap(role -> {
                    logger.info("The role of the user creating the new user is: " + role);
                    // Permitir tanto ADMIN como ADVISOR
                    if (!role.equals("ADMIN") && !role.equals("ADVISOR")) {
                        logger.error("Only ADMIN or ADVISOR users can create new users", null);
                        Map<String, String> errors = createErrorMap();
                        errors.put("Permission", "Only ADMIN or ADVISOR users can create new users");
                        return Mono.error(new BussinesException(ERROR_VALIDATION, errors));
                    }
                    return Mono.just(user);
                });
    }
    /**
     * Encripta la contraseña del usuario.
     */
    private Mono<User> encryptPassword(User user) {
        return passwordEncoder.encode(user.getPassword()) // devuelve Mono<String>
                .map(encodedPassword ->
                        user.toBuilder()
                                .password(encodedPassword) // aquí sí es un String
                                .build()
                );
    }





    /**
     * Actualiza un usuario existente en la base de datos.
     *
     * @param user El usuario a actualizar.
     * @return Un `Mono` que emite el usuario actualizado o un error si ocurre algún problema.
     */
    public Mono<User> updateUser(User user) {
        return txOperational.execute(() -> {
            logger.info("Attempting to update user with id: " + user.getId());
            return validate(user, this::validateUpdateUser)
                    .doOnError(e -> logger.error("Validation failed for user with id: " + user.getId(), e))
                    .flatMap(validatedUser ->
                            userRepository.existsByEmail(validatedUser.getEmail())
                                    .flatMap(exists -> {
                                        if (Boolean.TRUE.equals(exists)) {
                                            logger.error("Email already exists: " + validatedUser.getEmail(), null);
                                            Map<String, String> errors = createErrorMap();
                                            errors.put(FIELD_EMAIL, "The email address is already registered by another user");
                                            return Mono.error(new BussinesException(ERROR_VALIDATION , errors));
                                        }
                                        return userRepository.save(validatedUser)
                                                .doOnSuccess(updated -> logger.info("User updated successfully with id: " + updated.getId()));
                                    })
                    );
        });
    }

    /**
     * Obtiene todos los usuarios de la base de datos.
     *
     * @return Un `Flux` que emite los usuarios encontrados.
     */
    public Flux<User> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll()
                .doOnComplete(() -> logger.info("Finished fetching all users"));
    }

    /**
     * Obtiene todos los usuarios de la base de datos.
     *
     * @return Un `Flux` que emite los usuarios encontrados.
     */
    public Mono<User> getUserById(Long id) {
        logger.info("Fetching user by id: " + id);
        return userRepository.findById(id)
                .doOnSuccess(user -> {
                    if (user != null) {
                        logger.info("User found with id: " + id);
                    } else {
                        logger.info("No user found with id: " + id);
                    }
                });
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id El ID del usuario a eliminar.
     * @return Un `Mono` que completa cuando el usuario ha sido eliminado.
     */
    public Mono<Void> deleteUser(Long id) {
        logger.info("Deleting user with id: " + id);
        return userRepository.deleteById(id)
                .doOnSuccess(unused -> logger.info("User deleted successfully with id: " + id));
    }

    public Mono<Boolean> existsByDocumentAndEmail(String document, String email) {
        logger.info("Checking if user exists with document: " + document + "  email: " + email);
        return userRepository.existsByDocumentAndEmail(document, email)
                .doOnSuccess(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        logger.info("User already exists with document: " + document + "  email: " + email);
                    } else {
                        logger.info("No user exists with document: " + document + " and email: " + email);
                    }
                });
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
