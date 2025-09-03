package co.com.pragma.api.dto;

public record AuthDTO (
        String document,
        String token,
        String role
){
}
