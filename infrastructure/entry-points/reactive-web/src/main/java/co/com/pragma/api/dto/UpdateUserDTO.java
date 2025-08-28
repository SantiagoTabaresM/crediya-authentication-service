package co.com.pragma.api.dto;

import java.time.LocalDate;


public record UpdateUserDTO(
        Long id,
        String name,
        String lastName,
        String document,
        LocalDate birthDate,
        String address,
        String phone,
        String email,
        Integer baseSalary
) {}
