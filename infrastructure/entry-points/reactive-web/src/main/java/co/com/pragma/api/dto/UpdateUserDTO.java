package co.com.pragma.api.dto;

import java.util.Date;

public record UpdateUserDTO(
        Long id,
        String name,
        String lastName,
        String document,
        Date birthDate,
        String address,
        String phone,
        String email,
        Integer baseSalary
) {}
