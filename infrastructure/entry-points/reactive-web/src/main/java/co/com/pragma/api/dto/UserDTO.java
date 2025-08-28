package co.com.pragma.api.dto;


import java.time.LocalDate;


public record UserDTO(
        Long id,
        String name,
        String lastName,
        LocalDate birthDate,
        String address,
        String document,
        String phone,
        String email,
        Integer baseSalary
) {}
