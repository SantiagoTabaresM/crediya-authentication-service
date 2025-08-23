package co.com.pragma.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        String lastName,
        String document,
        String phone,
        @Email(message = "El correo debe tener un formato válido")
        String email,
        Integer baseSalary
) {}
