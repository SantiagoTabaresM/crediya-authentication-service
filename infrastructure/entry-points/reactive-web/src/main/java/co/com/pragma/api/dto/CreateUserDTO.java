package co.com.pragma.api.dto;

public record CreateUserDTO(
        String name,
        String lastName,
        String document,
        String phone,
        String email,
        Integer baseSalary
) {}
