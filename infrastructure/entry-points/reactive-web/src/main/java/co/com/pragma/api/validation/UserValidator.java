package co.com.pragma.api.validation;

import co.com.pragma.api.dto.CreateUserDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class UserValidator {

    private final ReactiveValidator reactiveValidator;

    public UserValidator(ReactiveValidator reactiveValidator) {
        this.reactiveValidator = reactiveValidator;
    }

    /**
     * Valida un CreateUserDTO de manera reactiva
     */
    public Mono<CreateUserDTO> validateCreateUser(CreateUserDTO userDTO) {
        return reactiveValidator.validate(userDTO, this::validateUserFields);
    }

    /**
     * Función de validación para CreateUserDTO
     */
    private Map<String, String> validateUserFields(CreateUserDTO userDTO) {
        Map<String, String> errors = ReactiveValidator.createErrorMap();

        // Validar nombres
        reactiveValidator.validateNotBlank(userDTO.name(), "nombres",
                "El nombre es obligatorio", errors);
        reactiveValidator.validateLength(userDTO.name(), "nombres", 2, 50, errors);

        // Validar apellidos
        reactiveValidator.validateNotBlank(userDTO.lastName(), "apellidos",
                "Los apellidos son obligatorios", errors);
        reactiveValidator.validateLength(userDTO.lastName(), "apellidos", 2, 50, errors);

        // Validar email
        reactiveValidator.validateEmail(userDTO.email(), "correo_electronico", errors);

        // Validar salario
        reactiveValidator.validateRange(userDTO.baseSalary(), "salario_base", 0, 15000000,
                "El salario base debe ser mayor a 0 y menor a 15000000", errors);

        return errors;
    }

    /**
     * Validación adicional para actualización (si es necesario)
     */
    public Mono<CreateUserDTO> validateForUpdate(CreateUserDTO userDTO) {
        return reactiveValidator.validate(userDTO, this::validateUpdateFields);
    }

    private Map<String, String> validateUpdateFields(CreateUserDTO userDTO) {
        Map<String, String> errors = validateUserFields(userDTO);
        // Aquí puedes agregar validaciones específicas para actualización
        return errors;
    }
}