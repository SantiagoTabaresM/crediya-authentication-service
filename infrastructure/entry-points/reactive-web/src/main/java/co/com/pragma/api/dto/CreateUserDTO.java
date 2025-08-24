package co.com.pragma.api.dto;


import java.time.LocalDate;

public record CreateUserDTO(
        String name,
        String lastName,
        LocalDate birthDate,
        String address,
        String document,
        String phone,
        String email,
        Integer baseSalary
) {}
