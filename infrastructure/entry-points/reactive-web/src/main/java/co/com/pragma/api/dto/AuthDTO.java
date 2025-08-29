package co.com.pragma.api.dto;

public record AuthDTO (
        String userId,
        String token,
        String role
){
}
