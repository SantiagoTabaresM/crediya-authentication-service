package co.com.pragma.api.dto;


public record UserDTO(
        Long id,
        String name,
        String lastName,
        String document,
        String phone,
        String email,
        Integer baseSalary
) {}
