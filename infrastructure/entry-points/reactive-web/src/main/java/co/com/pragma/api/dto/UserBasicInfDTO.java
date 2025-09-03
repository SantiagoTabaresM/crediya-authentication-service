package co.com.pragma.api.dto;

public record UserBasicInfDTO(
        String name,
        String lastName,
        Integer baseSalary,
        String document
)
{}
