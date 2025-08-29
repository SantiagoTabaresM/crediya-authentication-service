package co.com.pragma.model.auth;
import lombok.*;
//import lombok.NoArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Auth {
    private Integer userId;
    private String email;
    private String password;
    private Integer roleId;
}
