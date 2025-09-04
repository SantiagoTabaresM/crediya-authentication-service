package co.com.pragma.api.dto;


import java.time.LocalDate;

public record CreateUserDTO(
        String name,
        String lastName,
        LocalDate birthDate,
        String address,
        String document,
        Integer roleId,
        String phone,
        String email,
        String password,
        Integer baseSalary
) {}
