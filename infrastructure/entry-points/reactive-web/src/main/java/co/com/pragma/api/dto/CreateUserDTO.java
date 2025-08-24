package co.com.pragma.api.dto;


import java.util.Date;

public record CreateUserDTO(
        String name,
        String lastName,
        Date birthDate,
        String address,
        String document,
        String phone,
        String email,
        Integer baseSalary
) {}
