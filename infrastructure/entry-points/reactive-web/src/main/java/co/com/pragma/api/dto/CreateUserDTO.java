package co.com.pragma.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

public record CreateUserDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        String lastName,
        Date birthDate,
        String address,
        String document,
        String phone,
        @Email(message = "El correo debe tener un formato válido")
        String email,
        Integer baseSalary
) {}
